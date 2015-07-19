package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.clusters.ReplicaSetBuilder
import com.antwerkz.bottlerocket.configuration.Configuration
import com.antwerkz.bottlerocket.executable.Mongod
import com.jayway.awaitility.Awaitility
import com.mongodb.ReadPreference
import com.mongodb.ServerAddress
import org.bson.Document
import org.slf4j.LoggerFactory
import java.io.File
import java.util.ArrayList
import java.util.concurrent.TimeUnit
import kotlin.platform.platformStatic

class ReplicaSet(name: String = DEFAULT_NAME, port: Int = DEFAULT_PORT, version: String = DEFAULT_VERSION, baseDir: File = DEFAULT_BASE_DIR,
                 val size: Int = 3) : MongoCluster(name, port, version, baseDir) {

    public val nodes: MutableList<Mongod> = arrayListOf()
    private var nodeMap: Map<Int, Mongod> = hashMapOf()
    public var initialized: Boolean = false;
        private set

    companion object {
        private val LOG = LoggerFactory.getLogger(javaClass<ReplicaSet>())

        platformStatic fun builder(): ReplicaSetBuilder {
            return ReplicaSetBuilder()
        }

        platformStatic fun build(init: ReplicaSetBuilder.() -> Unit = {}): ReplicaSet {
            val builder = ReplicaSetBuilder()
            builder.init()
            return builder.build()
        }
    }

    init {
        var basePort = this.port
        for ( i in 0..size - 1) {
            val nodeName = "${name}-${basePort}"
            nodes.add(mongoManager.mongod(nodeName, basePort, File(baseDir, nodeName)))
            basePort += 1;
        }
        nodeMap = nodes.toMap { it.port }
    }

    override
    fun start() {
        for (node in nodes) {
            node.shutdown()
            node.config.replication.replSetName = name
            node.start()
        }

        initialize()
    }

    override fun isStarted(): Boolean {
        return nodes.filter { it.isAlive() }.count() != 0
    }

    fun addNode(node: Mongod) {
        nodes.add(node);
    }

    private fun initialize() {
        if (initialized) {
            return;
        }
        val primary = nodes.first()

        initiateReplicaSet(primary)
        LOG.info("replSet initiated.  waiting for primary.")
        waitForPrimary()
        LOG.info("primary found.  adding other members.")
        addMembers(nodes.asSequence().withIndex()
              .filter({ it.index > 0 })
              .map { it.value })

        waitForPrimary()
        initialized = true;

        LOG.info("replica set ${name} started.")
    }

    private fun initiateReplicaSet(mongod: Mongod) {
        val results = mongod.runCommand(Document("replSetInitiate",
              Document("_id", name)
                    .append("members", listOf(Document("_id", 1)
                          .append("host", "localhost:${mongod.port}"))
                    )), ReadPreference.primaryPreferred())
        if ( !(results?.getDouble("ok")?.toInt()?.equals(1) ?: false) ) {
            throw IllegalStateException("Failed to initiate replica set: ${results}")
        }
    }

    private fun addMembers(list: Sequence<Mongod>) {
        // used to be if(null) throw
        val primary = getPrimary() ?: throw IllegalStateException("Replica set ${name} has no primary")

        val config = primary.runCommand(Document("replSetGetConfig", 1)).get("config") as Document
        config.set("version", config.getInteger("version") + 1)
        val members: ArrayList<Document> = config.get("members") as ArrayList<Document>
        var id = members[0].getInteger("_id")
        list.map({
            Document("_id", ++id)
                  .append("host", "localhost:${it.port}")
        }).toCollection(members)

        val results = primary.runCommand(Document("replSetReconfig", config));

        if ( results.getDouble("ok").toInt() != 1) {
            throw RuntimeException("Failed to add members to replica set:  ${results}")
        }
    }

    fun getPrimary(): Mongod? {
        if (nodes.isEmpty()) {
            return null;
        }
        nodes.filter({ it.isAlive() }).forEach({ mongod ->
            val result = mongod.runCommand(Document("isMaster", null));

            if (result.containsKey ("primary")) {
                val host = result.getString("primary")
                return nodeMap.get(Integer.valueOf(host.split(":".toRegex()).toTypedArray()[1]))
            }
        })
        return null;
    }

    fun hasPrimary(): Boolean {
        return getPrimary() != null;
    }

    fun waitForPrimary(): Mongod? {
        Awaitility.await()
              .atMost(30, TimeUnit.SECONDS)
              .until({
                  try {
                      hasPrimary()
                  } catch(ignored: Exception) {
                  }
              })

        return getPrimary()
    }

    override
    fun clean() {
        nodes.forEach { it.clean() }
        baseDir.deleteTree()
    }

    override
    fun shutdown() {
        super.shutdown()
        val primary = getPrimary()
        nodes.filter { it != primary }.forEach { it.shutdown() }
        primary?.shutdown()
    }

    override
    fun enableAuth() {
        super.enableAuth()
        if (!isAuthEnabled()) {
            val mongod = nodes.first()
            mongod.start()
            mongod.addRootUser()
            mongod.shutdown()

            nodes.forEach { it.enableAuth(keyFile) }
            start()
        }
    }

    override fun updateConfig(update: Configuration) {
        nodes.forEach {
            it.config.merge(update)
        }
    }

    override fun allNodesActive() {
        val message = nodes.filter({ !it.tryConnect() })
              .map({ "mongod:${it.port} is not active" })
              .toArrayList()
              .join()
        if (!message.isEmpty()) {
            throw IllegalStateException(message)
        }
    }

    override fun getServerAddressList(): List<ServerAddress> {
        return nodes.map { it.getServerAddress() }
    }

    override fun isAuthEnabled(): Boolean {
        return nodes.map { it.isAuthEnabled() }.fold(true) { r, t -> r && t }
    }

    fun replicaSetUrl(): String {
        return "${name}/" + (nodes.map { "localhost:${it.port}" }.join(","))
    }
}

