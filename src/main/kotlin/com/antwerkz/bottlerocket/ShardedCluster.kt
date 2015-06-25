package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.executable.ConfigServer
import com.antwerkz.bottlerocket.executable.Mongos
import com.mongodb.ServerAddress
import org.bson.BsonDocument
import org.bson.codecs.BsonDocumentCodec
import org.bson.codecs.DecoderContext
import org.bson.json.JsonReader
import org.slf4j.LoggerFactory
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.platform.platformStatic

class ShardedCluster(name: String = DEFAULT_MONGOD_NAME, port: Int = DEFAULT_PORT,
                     version: String = DEFAULT_VERSION, baseDir: File = DEFAULT_BASE_DIR,
                     var shardCount: Int = 1, var mongosCount: Int = 1, var configSvrCount: Int = 1) :
      MongoCluster(name, port, version, baseDir) {

    var shards: MutableList<ReplicaSet> = arrayListOf()
    var mongoses: MutableList<Mongos> = arrayListOf()
    var configServers: MutableList<ConfigServer> = arrayListOf()
    private var nextPort = port
    private var initialized = false

    companion object {
        private val LOG = LoggerFactory.getLogger(javaClass<MongoExecutable>())

        platformStatic fun builder(): ShardedClusterBuilder {
            return ShardedClusterBuilder()
        }

        platformStatic fun build(init: ShardedClusterBuilder.() -> Unit = {}): ShardedCluster {
            val builder = ShardedClusterBuilder()
            builder.init()
            return builder.build()
        }
    }

    init {
        createMongoses()
        createShards()
        createConfigServers()
    }


    override
    fun start() {
        configServers.forEach { it.start() }
        shards.forEach { it.start() }
        mongoses.forEach { it.start() }

        if (!initialized) {
            shards.forEach { addMember(it) }
            initialized = true
        }
    }

    override
    fun enableAuth(pemFile: String) {
        shutdown()

        start()
        val mongos = mongoses.first()
        if (!adminAdded) {
            mongos.addRootUser()
            adminAdded = true
        }

        configServers.forEach { it.enableAuth(pemFile) }
        shards.forEach { replSet ->
            replSet.nodes.first().addRootUser()
            replSet.nodes.forEach { it.enableAuth(pemFile) }
            replSet.adminAdded = true
        }
        mongoses.forEach { it.enableAuth(pemFile) }

        shutdown()
        start()
    }

    private fun addMember(replicaSet: ReplicaSet) {
        val replSetUrl = replicaSet.replicaSetUrl();

        val results = runCommand(mongoses.first(), "sh.addShard(\"${replSetUrl}\");")

        if ( results.getInt32("ok").getValue() != 1) {
            throw RuntimeException("Failed to add ${replicaSet.name} to cluster:  ${results}")
        }
    }

    private fun createShards() {
        if (shards.isEmpty()) {
            for ( i in 0..shardCount - 1) {
                val replSetName = "${name}${i}"
                val replicaSet = ReplicaSet.builder()
                replicaSet.name = replSetName
                replicaSet.basePort = nextPort
                replicaSet.version = version
                replicaSet.baseDir = baseDir
                shards.add(replicaSet.build())
                nextPort += replicaSet.size;
            }
        }
    }

    private fun createConfigServers() {
        if (configServers.isEmpty()) {
            for ( i in 1..configSvrCount ) {
                val name = "configSvr-${nextPort}"
                val configSvr = mongoManager.configServer(name, nextPort, File(baseDir, name))
                configServers.add(configSvr)
                nextPort += 1;
            }
        }
    }

    private fun createMongoses() {
        if (mongoses.isEmpty()) {
            for ( i in 1..mongosCount) {
                val mongosName = "mongos-${nextPort}"
                mongoses.add(mongoManager.mongos(mongosName, nextPort, File(baseDir, mongosName), configServers))
                nextPort += 1;

            }
        }

    }

    public fun runCommand(mongos: Mongos, command: String): BsonDocument {
        val stream = ByteArrayOutputStream()
        val list = listOf(mongoManager.mongo,
              "admin", "--port", "${mongos.port}", "--quiet")
        ProcessExecutor()
              .command(list)
              .redirectOutput(stream)
              .redirectError(Slf4jStream.of(LoggerFactory.getLogger(javaClass)).asInfo())
              .redirectInput(ByteArrayInputStream(command.toByteArray()))
              .execute()

        val json = String(stream.toByteArray()).trim()
        try {
            return BsonDocumentCodec().decode(JsonReader(json), DecoderContext.builder().build())
        } catch(e: Exception) {
            LOG.error("Invalid response from server: ${json}")
            throw e;
        }
    }

    override
    fun shutdown() {
        mongoses.forEach { it.shutdown() }
        configServers.forEach { it.shutdown() }
        shards.forEach { it.shutdown() }
    }

    override
    fun clean() {
        shards.forEach { it.clean() }
        configServers.forEach { it.clean() }
        mongoses.forEach { it.clean() }
        baseDir.deleteTree()
    }

    override fun isAuthEnabled(): Boolean {
        return shards.map { it.isAuthEnabled() }.fold(true) { r, t -> r && t }
    }

    override fun getServerAddressList(): List<ServerAddress> {
        return mongoses.map { it.getServerAddress() }
    }
}

class ShardedClusterBuilder() {
    public var name: String = DEFAULT_MONGOD_NAME;
        set(value) {
            $name = value;
            baseDir = if (baseDir == DEFAULT_BASE_DIR) File("${TEMP_DIR}/${name}") else baseDir
        }
    public var basePort: Int = DEFAULT_PORT;
    public var version: String = DEFAULT_VERSION;
    public var shardCount: Int = 1;
    public var mongosCount: Int = 1;
    public var configSvrCount: Int = 1;
    public var baseDir: File = DEFAULT_BASE_DIR;

    fun build(): ShardedCluster {
        return ShardedCluster(name, basePort, version, baseDir, shardCount, mongosCount, configSvrCount)
    }

}