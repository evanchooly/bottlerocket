package com.antwerkz.bottlerocket.clusters

import com.antwerkz.bottlerocket.BottleRocket
import com.antwerkz.bottlerocket.configuration.Configuration
import com.antwerkz.bottlerocket.executable.ConfigServer
import com.antwerkz.bottlerocket.executable.Mongos
import com.github.zafarkhaja.semver.Version
import com.mongodb.ServerAddress
import org.bson.BsonDocument
import org.bson.Document
import org.bson.codecs.BsonDocumentCodec
import org.bson.codecs.DecoderContext
import org.bson.json.JsonReader
import org.slf4j.LoggerFactory
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File

class ShardedCluster @JvmOverloads constructor(
    name: String = BottleRocket.DEFAULT_NAME,
    port: Int = BottleRocket.DEFAULT_PORT,
    version: Version = BottleRocket.DEFAULT_VERSION,
    baseDir: File = BottleRocket.DEFAULT_BASE_DIR,
    var shardCount: Int = 1,
    var mongosCount: Int = 1,
    var configSvrCount: Int = 1
) : MongoCluster(name, port, version, baseDir) {
    var shards = arrayListOf<ReplicaSet>()
    var mongoses = arrayListOf<Mongos>()
    var configServers = arrayListOf<ConfigServer>()
    var nextPort = port
    var initialized = false

    companion object {
        private val LOG = LoggerFactory.getLogger(ShardedCluster::class.java)
        @JvmStatic
        fun builder(): ShardedClusterBuilder {
            return ShardedClusterBuilder()
        }

        @JvmStatic
        fun build(init: ShardedClusterBuilder.() -> Unit = {}): ShardedCluster {
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
        if (!isStarted()) {
            configServers.forEach { it.start() }
            shards.forEach { it.start() }
            mongoses.forEach { it.start() }

            if (!initialized) {
                shards.forEach { addMember(it) }
                initialized = true
            }
            super.start()
        }
    }

    override fun isStarted(): Boolean {
        val mongosAlive = mongoses.filter { it.isAlive() }.count() != 0
        val configServersAlive = configServers.filter { it.isAlive() }.count() != 0
        val shardsAlive = shards.filter { it.isStarted() }.count() != 0
        return mongosAlive && configServersAlive && shardsAlive
    }
/*
    override
    fun enableAuth() {
        super.enableAuth()
        configServers.forEach { mongoManager.enableAuth(it, keyFile) }
        shards.forEach { replSet ->
            replSet.nodes.forEach { mongoManager.enableAuth(it, keyFile) }
        }
        mongoses.forEach { mongoManager.enableAuth(it, keyFile) }
    }
*/
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

    private fun addMember(replicaSet: ReplicaSet) {
        val replSetUrl = replicaSet.replicaSetUrl()
        val results = mongoses.first().runCommand(Document("addShard", replSetUrl))

        if (results.getDouble("ok").toInt() != 1) {
            throw RuntimeException("Failed to add ${replicaSet.name} to cluster:  $results")
        }
    }

    private fun createShards() {
        if (shards.isEmpty()) {
            for (i in 0..shardCount - 1) {
                val replicaSet = ReplicaSet("$name$i", nextPort, version, baseDir)
                shards.add(replicaSet)
                nextPort += replicaSet.size
            }
        }
    }

    private fun createConfigServers() {
        if (configServers.isEmpty()) {
            for (i in 1..configSvrCount) {
                val name = "configSvr-$nextPort"
                val configSvr = mongoManager.configServer(name, nextPort, File(baseDir, name))
                configServers.add(configSvr)
                nextPort += 1
            }
        }
    }

    private fun createMongoses() {
        if (mongoses.isEmpty()) {
            for (i in 1..mongosCount) {
                val mongosName = "mongos-$nextPort"
                mongoses.add(mongoManager.mongos(mongosName, nextPort, File(baseDir, mongosName), configServers))
                nextPort += 1
            }
        }
    }

    fun runCommand(mongos: Mongos, command: String): BsonDocument {
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
        } catch (e: Exception) {
            LOG.error("Invalid response from server: $json", e)
            throw e
        }
    }

    override
    fun shutdown() {
        super.shutdown()
        shards.forEach { it.shutdown() }
        configServers.forEach { it.shutdown() }
        mongoses.forEach { it.shutdown() }
    }

    override fun isAuthEnabled(): Boolean {
        return shards.map { it.isAuthEnabled() }.fold(true) { r, t -> r && t }
    }

    override fun getServerAddressList(): List<ServerAddress> {
        return mongoses.map { it.getServerAddress() }
    }
}

class ShardedClusterBuilder() : MongoClusterBuilder<ShardedClusterBuilder>() {
    var shardCount: Int = 1
        private set
    var mongosCount: Int = 1
        private set
    var configSvrCount: Int = 1
        private set

    fun shardCount(value: Int): ShardedClusterBuilder {
        shardCount = value
        return this
    }

    fun mongosCount(value: Int): ShardedClusterBuilder {
        mongosCount = value
        return this
    }

    fun configSvrCount(value: Int): ShardedClusterBuilder {
        configSvrCount = value
        return this
    }

    fun build(): ShardedCluster {
        return ShardedCluster(name, port, version, baseDir, shardCount, mongosCount, configSvrCount)
    }
}