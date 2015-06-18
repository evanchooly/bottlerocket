package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.executable.ConfigServer
import com.antwerkz.bottlerocket.executable.Mongos
import com.mongodb.ServerAddress
import org.apache.commons.lang3.SystemUtils
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
import java.net.InetAddress
import kotlin.platform.platformStatic

class ShardedCluster(name: String = DEFAULT_MONGOD_NAME, port: Int = DEFAULT_PORT,
                     version: String = DEFAULT_VERSION, public var size: Int = 3,
                     baseDir: File = DEFAULT_BASE_DIR) : MongoCluster(name, port, version, baseDir) {

    var shards: MutableList<ReplicaSet> = arrayListOf()
    var mongoses: MutableList<Mongos> = arrayListOf()
    var configServers: MutableList<ConfigServer> = arrayListOf()
    private var nextPort = port

    companion object {
        private val LOG = LoggerFactory.getLogger(javaClass<MongoExecutable>())

        platformStatic fun builder(): ShardedClusterBuilder {
            return ShardedClusterBuilder()
        }
    }

    override
    fun start() {
        createMongoses()
        createShards()
        createConfigServers()

        configServers.forEach { it.start()}
        shards.forEach { it.start() }
        mongoses.forEach { it.start()}

        shards.forEach { addMember(it) }
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
            for ( i in 0..size - 1) {
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
            for ( i in 1..3 ) {
                val name = "configSvr-${i}"
                val configSvr = mongoManager.configServer(name, nextPort, File(baseDir, name))
                configServers.add(configSvr)
                nextPort += 1;
            }
        }
    }

    private fun createMongoses() {
        if (mongoses.isEmpty()) {
            for ( i in 1..size) {
                val mongosName = "mongos-${i}"
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
        shards.forEach { it.shutdown() }
        configServers.forEach { it.shutdown()}
        mongoses.forEach { it.shutdown()}
    }

    override
    fun clean() {
        shards.forEach { it.clean() }
        configServers.forEach { it.clean()}
        mongoses.forEach { it.clean()}
        baseDir.deleteTree()
    }

    override
    fun enableAuth() {
        shards.forEach {
            it.enableAuth()
        }
    }

    override fun authEnabled(): Boolean {
        return shards.map { it.authEnabled() }.fold(true) { r, t -> r && t}
    }

    override fun getServerAddressList(): List<ServerAddress> {
        return shards.flatMap { it.getServerAddressList() }
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
    public var size: Int = 3;
    public var baseDir: File = DEFAULT_BASE_DIR;

    fun build(): ShardedCluster {
        return ShardedCluster(name, basePort, version, size, baseDir)
    }

}