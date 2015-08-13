package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.clusters.ReplicaSetBuilder
import com.antwerkz.bottlerocket.configuration.mongo22.Configuration22
import com.antwerkz.bottlerocket.configuration.mongo24.Configuration24
import com.antwerkz.bottlerocket.configuration.mongo26.Configuration26
import com.antwerkz.bottlerocket.configuration.mongo30.Configuration30
import com.antwerkz.bottlerocket.executable.Mongod
import com.jayway.awaitility.Awaitility
import com.mongodb.ServerAddress
import org.bson.Document
import org.slf4j.LoggerFactory
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.platform.platformStatic

class ReplicaSet(name: String = DEFAULT_NAME, port: Int = DEFAULT_PORT, version: String = DEFAULT_VERSION, baseDir: File = DEFAULT_BASE_DIR,
                 val size: Int = 3) : MongoCluster(name, port, version, baseDir) {

    public val nodes: MutableList<Mongod> = arrayListOf()
    private var nodeMap: Map<Int, Mongod> = hashMapOf()
    public var initialized: Boolean = false;

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
            mongoManager.setReplicaSetName(node, name);
            val configuration = node.config
            if (configuration is Configuration30) {
                configuration.replication.replSetName = name
            } else if (configuration is Configuration26) {
                configuration.replication.replSetName = name
            } else if (configuration is Configuration24) {
                configuration.replSet = name
            } else if (configuration is Configuration22) {
                configuration.replSet = name
            }
            node.start()
        }

        mongoManager.initialize(this)
    }

    override fun isStarted(): Boolean {
        return nodes.filter { it.isAlive() }.count() != 0
    }

    fun addNode(node: Mongod) {
        nodes.add(node);
    }

    fun getPrimary(): Mongod? {
        try {
            if (nodes.isEmpty()) {
                return null;
            }
            nodes
                  .filter({ it.isAlive() })
                  .forEach({ mongod ->
                      val result = mongod.runCommand(Document("isMaster", null));

                      if (result.containsKey ("primary")) {
                          val host = result.getString("primary")
                          return nodeMap.get(Integer.valueOf(host.split(":".toRegex()).toTypedArray()[1]))
                      }
                  })
            return null;
        } catch(e: Exception) {
            LOG.error(e.getMessage(), e)
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

    override fun updateConfig(update: Configuration30) {
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

