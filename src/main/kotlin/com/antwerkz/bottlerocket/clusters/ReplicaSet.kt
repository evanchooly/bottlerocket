package com.antwerkz.bottlerocket.clusters

import com.antwerkz.bottlerocket.BottleRocket
import com.antwerkz.bottlerocket.configuration.Configuration
import com.antwerkz.bottlerocket.executable.Mongod
import com.antwerkz.bottlerocket.runCommand
import com.github.zafarkhaja.semver.Version
import com.jayway.awaitility.Awaitility
import com.mongodb.ReadPreference
import com.mongodb.ServerAddress
import org.bson.Document
import java.io.File
import java.util.concurrent.TimeUnit.SECONDS

class ReplicaSet @JvmOverloads constructor(
    clusterRoot: File = BottleRocket.DEFAULT_BASE_DIR,
    name: String = BottleRocket.DEFAULT_NAME,
    version: Version = BottleRocket.DEFAULT_VERSION,
    allocator: PortAllocator = PortAllocator(),
) : MongoCluster(clusterRoot, name, version, allocator) {
    private var nodeMap = hashMapOf<Int, Mongod>()
    var initialized: Boolean = false

    init {
        configure {
            replication {
                oplogSizeMB = 10
                replSetName = name
            }
        }
    }

    override
    fun start() {
        if (!isStarted()) {
            if (nodeMap.isEmpty()) {
                addNode()
            }

            for (node in nodeMap.values) {
                node.start()
            }

            initialize()
            super.start()
        }
    }

    override fun isStarted(): Boolean {
        return nodeMap.values.filter { it.isAlive() }.count() != 0
    }

    fun addNode(config: Configuration = Configuration()) {
        val port = config.net?.port ?: allocator.next()
        val nodeName = "$name-node${nodeMap.size}"
        val node = mongoManager.mongod(File(clusterRoot, nodeName), nodeName, port)
        node.configure(configuration.update(config))

        nodeMap.put(node.port, node)
    }

    fun getPrimary(): Mongod? {
        nodeMap.values
            .filter { it.isAlive() }
            .forEach { mongod ->
                val result = mongod.getClient()
                    .runCommand(Document("isMaster", null))

                if (result.containsKey("primary")) {
                    return nodeMap[result.getString("primary").substringAfter(":").toInt()]
                }
            }
        return null
    }

    fun hasPrimary(): Boolean {
        return getPrimary() != null
    }

    fun waitForPrimary(): Mongod? {
        Awaitility.await("Waiting for primary in $clusterRoot")
            .atMost(30, SECONDS)
            .until<Boolean>({
                hasPrimary()
            })

        return getPrimary()
    }

    override
    fun shutdown() {
        val primary = getPrimary()
        nodeMap.values
            .filter { it != primary }
            .forEach(Mongod::shutdown)
        primary?.shutdown()
        super.shutdown()
    }

    /*
        override
        fun enableAuth() {
            super.enableAuth()
            nodes.forEach { mongoManager.enableAuth(it, keyFile) }
        }
    */
    fun initialize() {
        val first = nodeMap.values.filter { it.isAlive() }.firstOrNull() ?: throw IllegalStateException("No servers found")
        val replicaSetConfig = mongoManager.getReplicaSetConfig(first.getClient())
        if (!initialized && replicaSetConfig == null) {
            initiateReplicaSet()
            logger.info("replSet initiated.  waiting for primary.")
            waitForPrimary()
            logger.info("primary found.  adding other members.")
            addMemberNodes()
            val waitForPrimary = waitForPrimary()

            if (getPrimary() == null) {
                throw IllegalStateException("Should have found a primary node.")
            }
            logger.info("replica set $name started.")
        }
    }

    fun initiateReplicaSet() {
        val primary = nodeMap.values.first()
        val results = primary.getClient()
            .runCommand(Document("replSetInitiate", Document("_id", name)
                .append("members", listOf(Document("_id", 1)
                    .append("host", "localhost:${primary.port}"))
                )), ReadPreference.primaryPreferred())
        if (!(results.getDouble("ok")?.toInt()?.equals(1) ?: false)) {
            throw IllegalStateException("Failed to initiate replica set: $results")
        }
        initialized = true
    }

    private fun addMemberNodes() {
        val client = getAdminClient()
        val config = mongoManager.getReplicaSetConfig(client)
        if (config != null) {
            config.set("version", config.getInteger("version") + 1)
            val members = config.getList("members", Document::class.java)
            var id = members[0].getInteger("_id")
            nodeMap.values.asSequence().withIndex()
                .filter { it.index > 0 }
                .map { Document("_id", ++id).append("host", "localhost:${it.value.port}") }
                .toCollection(members)
            val results = client.runCommand(Document("replSetReconfig", config))

            if (results.getDouble("ok").toInt() != 1) {
                throw RuntimeException("Failed to add members to replica set:  $results")
            }
        } else {
            throw IllegalStateException("No replica set configuration found")
        }
    }

    override fun configure(update: Configuration) {
        super.configure(update)
        nodeMap.values.forEach {
            it.configure(update)
        }
    }

    override fun getServerAddressList(): List<ServerAddress> {
        return nodeMap.values.map { it.getServerAddress() }
    }

    override fun isAuthEnabled(): Boolean {
        return nodeMap.values.map { it.isAuthEnabled() }.fold(true) { r, t -> r && t }
    }

    fun replicaSetUrl(): String {
        return "$name/" + (nodeMap.values.joinToString(",") { "localhost:${it.port}" })
    }
}