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
import java.io.File
import java.net.InetAddress
import java.util.concurrent.TimeUnit

class ReplicaSet(name: String = DEFAULT_MONGOD_NAME, port: Int,
                    version: String, public var size: Int): MongoCluster(name, port, version) {
    public var nodes: MutableList<Mongod> = arrayListOf()
    private var nodeMap: Map<Int, Mongod> = hashMapOf()
    public var initialized: Boolean = false;
        private set
    private var client: MongoClient? = null;


    fun start() {
        if (nodes.isEmpty()) {
            var port = basePort
            for ( i in 0..size - 1) {
                val nodeName = "${name}${i}"
                val mongod = Mongod(nodeName, port, version)
                mongod.logDir = File(logDir, nodeName)
                mongod.dataDir = File(dataDir, nodeName)
                mongod.replSetName = name
                nodes.add(mongod)
                port += 1;
            }
        }
        for (node in nodes) {
            node.start()
            node.replicaSet = this;
        }
        nodeMap = nodes.toMap { it.port }

        initialize()
    }

    private fun initialize() {
        if (initialized) {
            return;
        }
        val primary = nodes.first()

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