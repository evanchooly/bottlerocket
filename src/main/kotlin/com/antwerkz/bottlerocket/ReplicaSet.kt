package com.antwerkz.bottlerocket

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import com.jayway.awaitility.Awaitility
import com.mongodb.MongoClient
import com.mongodb.MongoClientOptions
import com.mongodb.ReplicaSetStatus
import org.apache.commons.lang3.SystemUtils
import org.slf4j.LoggerFactory
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream
import java.io.ByteArrayInputStream
import java.net.InetAddress
import java.util.concurrent.TimeUnit

class ReplicaSet(cluster: MongoCluster, public val name: String, public var size: Int): Commandable {
    public var nodes: MutableList<Mongod> = arrayListOf()
    private var nodeMap: Map<Int, Mongod> = hashMapOf()
    public var initialized: Boolean = false;
        private set
    private var client: MongoClient? = null;

    init {
        var port = cluster.basePort
        for ( i in 0..size - 1) {
            val mongod = Mongod("${name}${i}", port, cluster.version, cluster.dataDir, cluster.logDir)
            mongod.replSetName = cluster.name
            nodes.add(mongod)
            port += 1;
        }
    }

    private fun initialize() {
        if (initialized) {
            return;
        }
        val primary = nodes.get(0)
        primary.start()

        initiateReplicaSet(primary)
        nodes.sequence().withIndex()
              .filter { it.index > 0 }
              .forEach { addMember(primary, it.value) }

        waitForPrimary()
        initialized = true;
    }

    private fun initiateReplicaSet(mongod: Mongod): Boolean {
        return runCommand(mongod, "rs.initiate();\nrs.status();\nquit;\n")
    }

    private fun addMember(primary: Mongod, mongod: Mongod): Boolean {
        return runCommand(primary, "rs.add(\"${InetAddress.getLocalHost().getHostName()}:${mongod.port}\");")
    }

    fun getNode(port: Int): Mongod? {
        return nodeMap.get(port)
    }

    fun getPrimary(): Mongod? {
        return nodeMap.get(getClient().getReplicaSetStatus().getMaster()?.getPort());
    }

    fun hasPrimary(): Boolean {
        return getPrimary() != null;
    }

    fun waitForPrimary() {
        Awaitility.await()
            .atMost(30, TimeUnit.SECONDS)
            .until({ while (!hasPrimary()) { Thread.sleep(1000) } })
    }

    fun getClient(): MongoClient {
        if (client == null) {
            client = MongoClient(nodes.map { it.getServerAddress() },
                  MongoClientOptions.builder()
                        .connectTimeout(3000)
                        .build())
        }

        return client!!;
    }

    fun start() {
        for (node in nodes) {
            node.start()
            node.replicaSet = this;
        }
        nodeMap = nodes.toMap { it.port }

        initialize()
    }

    fun shutdown() {
        for (node in nodes) {
            node.shutdown()
        }
        if (client != null) {
            client?.close()
            client = null;
        }
    }
}