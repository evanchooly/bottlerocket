package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.clusters.ReplicaSetBuilder
import com.antwerkz.bottlerocket.configuration.Configuration
import com.antwerkz.bottlerocket.configuration.mongo26.configuration
import com.antwerkz.bottlerocket.executable.Mongod
import com.jayway.awaitility.Awaitility
import com.mongodb.ReadPreference
import com.mongodb.ServerAddress
import org.bson.Document
import org.slf4j.LoggerFactory
import java.io.File
import java.util.ArrayList
import java.util.concurrent.TimeUnit

class ReplicaSet public @JvmOverloads constructor(name: String = BottleRocket.DEFAULT_NAME,
                                                  port: Int = BottleRocket.DEFAULT_PORT,
                                                  version: String = BottleRocket.DEFAULT_VERSION,
                                                  baseDir: File = BottleRocket.DEFAULT_BASE_DIR,
                                                  val size: Int = 3) :
        MongoCluster(name, port, version, baseDir) {

    public val nodes: MutableList<Mongod> = arrayListOf()
    private var nodeMap = hashMapOf<Int, Mongod>()
    public var initialized: Boolean = false;

    companion object {
        private val LOG = LoggerFactory.getLogger(ReplicaSet::class.java)

        @JvmStatic fun builder(): ReplicaSetBuilder {
            return ReplicaSetBuilder()
        }

        @JvmStatic fun build(init: ReplicaSetBuilder.() -> Unit = {}): ReplicaSet {
            val builder = ReplicaSetBuilder()
            builder.init()
            return builder.build()
        }
    }

    init {
        var basePort = this.port
        for (i in 0..size - 1 /*step 3*/) {
            val nodeName = "${name}-${basePort}"
            val node = mongoManager.mongod(nodeName, basePort, File(baseDir, nodeName))
            setReplicaSetName(node, name);
            nodes.add(node)
            basePort += 1;
        }
        nodeMap.putAll(nodes.toMapBy { it.port })
    }

    override
    fun start() {
        if (!isStarted()) {
            for (node in nodes) {
                node.start(name)
            }

            initialize()
        }
    }

    override fun isStarted(): Boolean {
        return nodes.filter { it.isAlive() }.count() != 0
    }

    fun addNode(node: Mongod) {
        nodes.add(node);
        nodeMap.put(node.port, node)
    }

    fun getPrimary(): Mongod? {
        try {
            nodes.filter({ it.isAlive() })
                    .forEach({ mongod ->
                        val mongoClient = mongod.getClient(isAuthEnabled())
                        val result = mongoClient.runCommand(Document("isMaster", null));

                        if (result.containsKey ("primary")) {
                            val host = result.getString("primary")
                            return nodeMap.get(Integer.valueOf(host.split(":".toRegex()).toTypedArray()[1]))
                        }
                    })
            return null;
        } catch(e: Exception) {
            LOG.error(e.message, e)
            return null
        }
    }

    fun hasPrimary(): Boolean {
        return getPrimary() != null;
    }

    fun waitForPrimary(): Mongod? {
        Awaitility.await()
                .atMost(30, TimeUnit.SECONDS)
                .until<Boolean>({
                    hasPrimary()
                })

        return getPrimary()
    }

    override
    fun shutdown() {
        val primary = getPrimary()
        nodes.filter { it != primary }.forEach { it.shutdown() }
        primary?.shutdown()
        super.shutdown()
    }

    override
    fun enableAuth() {
        super.enableAuth()
        nodes.forEach { mongoManager.enableAuth(it, keyFile) }
    }

    fun initialize() {
        val first = nodes.filter { it.isAlive() }.first()
        val replicaSetConfig = mongoManager.getReplicaSetConfig(first.getClient())
        if (replicaSetConfig == null) {
            initiateReplicaSet()
            LOG.info("replSet initiated.  waiting for primary.")
            waitForPrimary()
            LOG.info("primary found.  adding other members.")
            addMembers()

            waitForPrimary()

            LOG.info("replica set ${name} started.")
        }
    }

    fun initiateReplicaSet() {
        val primary = nodes.first()
        val results = primary.getClient(isAuthEnabled())
                .runCommand(Document("replSetInitiate", Document("_id", name)
                        .append("members", listOf(Document("_id", 1)
                                .append("host", "localhost:${primary.port}"))
                        )), ReadPreference.primaryPreferred())
        if ( !(results.getDouble("ok")?.toInt()?.equals(1) ?: false) ) {
            throw IllegalStateException("Failed to initiate replica set: ${results}")
        }
        initialized = true;
    }

    private fun addMembers() {
        val client = getAdminClient()
        val config = mongoManager.getReplicaSetConfig(client)
        if (config != null) {
            config.set("version", config.getInteger("version") + 1)
            val members = config.get("members") as ArrayList<Document>
            var id = members[0].getInteger("_id")
            nodes.asSequence().withIndex()
                    .filter({ it.index > 0 })
                    .map { Document("_id", ++id).append("host", "localhost:${it.value.port}") }
                    .toCollection(members)

            val results = client.runCommand(Document("replSetReconfig", config));

            if ( results.getDouble("ok").toInt() != 1) {
                throw RuntimeException("Failed to add members to replica set:  ${results}")
            }
        } else {
            throw IllegalStateException("No replica set configuration found")
        }
    }

    fun setReplicaSetName(node: Mongod, name: String) {
        node.config.merge(configuration {
            replication {
                replSetName = name
            }
        })
    }

    override fun updateConfig(update: Configuration) {
        nodes.forEach {
            it.config.merge(update)
        }
    }

    override fun getServerAddressList(): List<ServerAddress> {
        return nodes.map { it.getServerAddress() }
    }

    override fun isAuthEnabled(): Boolean {
        return nodes.map { it.isAuthEnabled() }.fold(true) { r, t -> r && t }
    }

    fun replicaSetUrl(): String {
        return "${name}/" + (nodes.map { "localhost:${it.port}" }.joinToString(","))
    }
}

