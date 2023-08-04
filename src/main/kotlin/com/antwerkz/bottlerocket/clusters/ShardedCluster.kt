package com.antwerkz.bottlerocket.clusters

import com.antwerkz.bottlerocket.BottleRocket
import com.antwerkz.bottlerocket.configuration.Configuration
import com.antwerkz.bottlerocket.configuration.configuration
import com.antwerkz.bottlerocket.configuration.types.ClusterRole.configsvr
import com.antwerkz.bottlerocket.configuration.types.ClusterRole.shardsvr
import com.antwerkz.bottlerocket.executable.Mongos
import com.github.zafarkhaja.semver.Version
import com.mongodb.MongoClientSettings.Builder
import com.mongodb.ServerAddress
import com.mongodb.client.MongoClient
import java.io.File

class ShardedCluster
@JvmOverloads
constructor(
    version: Version = BottleRocket.DEFAULT_VERSION,
    name: String = BottleRocket.DEFAULT_NAME,
    baseDir: File = BottleRocket.DEFAULT_BASE_DIR,
    allocator: PortAllocator = BottleRocket.PORTS
) : MongoCluster(baseDir, name, version, allocator) {
    val shards = arrayListOf<ReplicaSet>()
    val mongoses = arrayListOf<Mongos>()
    val configServer: ReplicaSet
    var initialized = false

    init {
        configServer =
            ReplicaSet(
                version,
                "configserver",
                File(clusterRoot, "configserver"),
                allocator = allocator
            )
        configServer.configure { sharding { clusterRole = configsvr } }
        addShard()
        addMongos()
    }

    override fun getClient(builder: Builder): MongoClient {
        return mongoses.first().getClient()
    }

    override fun start() {
        if (!isStarted()) {
            configServer.start()

            shards.forEach { it.start() }

            mongoses.forEach {
                it.configure { sharding { configDB = configServer.replicaSetUrl() } }
                it.start()
            }

            if (!initialized) {
                shards.forEach { addMember(it) }
                initialized = true
            }
            super.start()
        }
    }

    private fun addMember(replicaSet: ReplicaSet) {
        replicaSet.configure(configuration)
        val replSetUrl = replicaSet.replicaSetUrl()
        val results = mongoses.first().runCommand("{ addShard: '$replSetUrl' }")

        if (results.getDouble("ok").toInt() != 1) {
            throw RuntimeException("Failed to add ${replicaSet.name} to cluster:  $results")
        }
    }
    override fun isStarted(): Boolean {
        return mongoses.any { it.isAlive() } &&
            configServer.isStarted() &&
            shards.any { it.isStarted() }
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
    override fun configure(update: Configuration) {
        super.configure(update)
        shards.forEach { it.configure(update) }
        mongoses.forEach { it.configure(update) }
        configServer.configure(update)
    }

    fun addShard(
        shard: ReplicaSet =
            ReplicaSet(
                version,
                "shard${shards.size}",
                File(clusterRoot, "shard${shards.size}"),
                allocator
            )
    ) {
        shard.configure(configuration)
        shard.configure {
            sharding {
                clusterRole = shardsvr
                configDB = configServer.replicaSetUrl()
            }
        }
        shards += shard
    }

    fun addMongos(config: Configuration = configuration {}) {
        val port = allocator.next()
        val nodeName = "$name-$port"
        val mongos =
            mongoManager.mongos(File(File(clusterRoot, "mongos"), nodeName), nodeName, port)
        mongos.configure(configuration)
        mongos.configure(config)
        mongoses += mongos
    }

    override fun shutdown() {
        super.shutdown()
        mongoses.forEach { it.shutdown() }
        shards.forEach { it.shutdown() }
        configServer.shutdown()
    }

    override fun isAuthEnabled(): Boolean {
        return shards.map { it.isAuthEnabled() }.fold(true) { r, t -> r && t }
    }

    override fun getServerAddressList(): List<ServerAddress> {
        return mongoses.map { it.getServerAddress() }
    }
}
