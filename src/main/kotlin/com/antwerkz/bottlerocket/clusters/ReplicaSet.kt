package com.antwerkz.bottlerocket.clusters

import com.antwerkz.bottlerocket.BottleRocket
import com.antwerkz.bottlerocket.configuration.Configuration
import com.antwerkz.bottlerocket.doc
import com.antwerkz.bottlerocket.executable.Mongod
import com.antwerkz.bottlerocket.runCommand
import com.github.zafarkhaja.semver.Version
import com.jayway.awaitility.Awaitility
import com.jayway.awaitility.Duration
import com.mongodb.ServerAddress
import java.io.File
import java.util.concurrent.TimeUnit.SECONDS
import org.bson.Document

class ReplicaSet
@JvmOverloads
constructor(
    version: Version = BottleRocket.DEFAULT_VERSION,
    name: String = BottleRocket.DEFAULT_NAME,
    clusterRoot: File = BottleRocket.DEFAULT_BASE_DIR,
    allocator: PortAllocator = BottleRocket.PORTS,
) : MongoCluster(clusterRoot, name, version, allocator) {
    private var members = mutableListOf<Mongod>()
    private var initialized: Boolean = false

    init {
        addNode()
        configure {
            replication {
                oplogSizeMB = 10
                replSetName = name
            }
        }
    }

    override fun start() {
        if (!isStarted()) {
            for (node in members) {
                node.start()
            }

            initialize()
            Awaitility.await()
                .atMost(Duration.TEN_SECONDS)
                .pollInterval(Duration.FIVE_HUNDRED_MILLISECONDS)
                .until<Boolean> {
                    members
                        .map { getClient() }
                        .any { it.runCommand("{ isMaster: null }").getBoolean("ismaster", false) }
                }
            super.start()
        }
    }

    override fun isStarted(): Boolean {
        return members.any { it.isAlive() }
    }

    fun addNode(config: Configuration = Configuration()) {
        val port = config.net?.port ?: allocator.next()
        val nodeName = "$name-node${members.size}"
        val node = mongoManager.mongod(File(clusterRoot, nodeName), nodeName, port)
        node.configure(configuration.update(config))

        members += node
    }

    fun getPrimary(): Mongod? {
        return members
            .filter { it.isAlive() }
            .firstOrNull { mongod ->
                mongod.getClient().runCommand("{ isMaster: null }").containsKey("primary")
            }
    }

    fun hasPrimary(): Boolean = getPrimary() != null

    fun waitForPrimary(): Mongod? {
        Awaitility.await("Waiting for primary in $clusterRoot").atMost(30, SECONDS).until<Boolean> {
            hasPrimary()
        }

        return getPrimary()
    }

    override fun shutdown() {
        val primary = getPrimary()
        members.filter { it != primary }.forEach(Mongod::shutdown)
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
    private fun initialize() {
        val first =
            members.firstOrNull { it.isAlive() } ?: throw IllegalStateException("No servers found")
        val replicaSetConfig = mongoManager.getReplicaSetConfig(first.getClient())
        if (!initialized && replicaSetConfig == null) {
            initiateReplicaSet()
            logger.info("replSet initiated.  waiting for primary.")
            waitForPrimary()
            logger.info("primary found.  adding other members.")
            addMemberNodes()
            waitForPrimary() ?: throw IllegalStateException("Should have found a primary node.")

            logger.info("replica set $name started.")
        }
    }

    private fun initiateReplicaSet() {
        val primary = members.first()
        val results =
            primary
                .getClient()
                .runCommand(
                    """{
                'replSetInitiate': {
                '_id': '$name', 
                'members': [ 
                    { 
                        '_id': 0,
                        'host': 'localhost:${primary.port}'
                    } ],
                }
            }"""
                )
        if (!(results.getDouble("ok")?.toInt()?.equals(1) ?: false)) {
            throw IllegalStateException("Failed to initiate replica set: $results")
        }
        initialized = true
    }

    private fun addMemberNodes() {
        if (members.size > 1) {
            val client = adminClient
            val config = mongoManager.getReplicaSetConfig(client)
            if (config != null) {
                config["version"] = config.getInteger("version") + 1
                val members = config.getList("members", Document::class.java)
                var id = members[0].getInteger("_id")
                this.members
                    .withIndex()
                    .filter { it.index > 0 }
                    .map { "{ _id: ${++id}, host: localhost:${it.value.port}".doc() }
                    .toCollection(members)
                val results = client.runCommand(Document("replSetReconfig", config))

                if (results.getDouble("ok").toInt() != 1) {
                    throw RuntimeException("Failed to add members to replica set:  $results")
                }
            } else {
                throw IllegalStateException("No replica set configuration found")
            }
        }
    }

    override fun configure(update: Configuration) {
        super.configure(update)
        members.forEach { it.configure(update) }
    }

    override fun getServerAddressList(): List<ServerAddress> {
        return members.map { it.getServerAddress() }
    }

    override fun isAuthEnabled(): Boolean {
        return members.map { it.isAuthEnabled() }.fold(true) { r, t -> r && t }
    }

    fun replicaSetUrl(): String {
        return "$name/" + (members.joinToString(",") { "localhost:${it.port}" })
    }
}
