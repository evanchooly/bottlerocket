package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.clusters.ShardedClusterBuilder
import com.antwerkz.bottlerocket.configuration.Configuration
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

class ShardedCluster(name: String = DEFAULT_NAME, port: Int = DEFAULT_PORT,
                     version: String = DEFAULT_VERSION, baseDir: File = DEFAULT_BASE_DIR,
                     var shardCount: Int = 1, var mongosCount: Int = 1, var configSvrCount: Int = 1) :
      MongoCluster(name, port, version, baseDir) {

    var shards: MutableList<ReplicaSet> = arrayListOf()
    var mongoses: MutableList<Mongos> = arrayListOf()
    var configServers: MutableList<ConfigServer> = arrayListOf()
    var nextPort = port
    var initialized = false

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

    override fun isStarted(): Boolean {
        val mongosAlive = mongoses.filter { it.isAlive() }.count() != 0
        val configServersAlive = configServers.filter { it.isAlive() }.count() != 0
        val shardsAlive = shards.filter { it.isStarted() }.count() != 0
        return mongosAlive && configServersAlive && shardsAlive
    }

    override
    fun enableAuth() {
        super.enableAuth()
        shutdown()

        start()
        val mongos = mongoses.first()
        if (!adminAdded) {
            mongos.addRootUser()
            adminAdded = true
        }

        configServers.forEach { it.enableAuth(keyFile) }
        shards.forEach { replSet ->
            replSet.nodes.first().addRootUser()
            replSet.nodes.forEach { it.enableAuth(keyFile) }
            replSet.adminAdded = true
        }
        mongoses.forEach { it.enableAuth(keyFile) }

        shutdown()
        start()
    }

    override fun updateConfig(update: Configuration) {
        shards.forEach {
            it.updateConfig(update)
        }
        mongoses.forEach {
            it.config.merge(update)
        }
        mongoses.forEach {
            it.config.merge(update)
        }
    }

    override fun allNodesActive() {
        var list = shards.flatMap { it.nodes }
              .filter { !it.tryConnect() }
              .map({ "mongod:${it.port} is not active" })
              .toArrayList()

        configServers.filter { !it.tryConnect() }
              .map({ "configSvr:${it.port} is not active" })
              .toCollection(list)

        var message = mongoses.filter { !it.tryConnect() }
              .map({ "mongos:${it.port} is not active" })
              .toCollection(list)
              .join()
        if (!message.isEmpty()) {
            throw IllegalStateException(message)
        }
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
                val replicaSet = ReplicaSet("${name}${i}", nextPort, version, baseDir)
                shards.add(replicaSet)
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
        super.shutdown()
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