package com.antwerkz.bottlerocket

import com.jayway.awaitility.Awaitility
import com.mongodb.MongoClient
import com.mongodb.MongoClientOptions
import java.io.File
import java.net.InetAddress
import java.util.Date
import java.util.concurrent.Callable
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
        println("replSet initiated.  waiting for primary.")
        waitForPrimary()
        println("primary found.  adding other members.")
        nodes.sequence().withIndex()
              .filter { it.index > 0 }
              .forEach { addMember(it.value) }

        waitForPrimary()
        initialized = true;

        println("replica set ${name} started.")
    }

    private fun initiateReplicaSet(mongod: Mongod) {
        val results = runCommand(mongod, "rs.initiate();")

        if( !(results.getInt32("ok")?.intValue()?.equals(1) ?: false) ) {
            throw IllegalStateException("Failed to initiate replica set: ${results}")
        }
    }

    private fun addMember(mongod: Mongod) {
        val primary = getPrimary()
        if (primary == null) {
            throw IllegalStateException("Replica set ${name} has no primary")
        }
        val results = runCommand(primary, "rs.add(\"${InetAddress.getLocalHost().getHostName()}:${mongod.port}\");")

        if( results.getInt32("ok").getValue() != 1) {
            throw RuntimeException("Failed to add member to replica set:  ${mongod}")
        }
    }

    fun getNode(port: Int): Mongod? {
        return nodeMap.get(port)
    }

    fun getPrimary(): Mongod? {
        if (nodes.isEmpty()) {
            return null;
        }
        val mongod = nodes.filter({ it.isRunning() }).first()
        val result = runCommand(mongod, "db.isMaster()");

        if (result.containsKey ("primary")) {
            val host = result.getString("primary")!!.getValue()
            return nodeMap.get(Integer.valueOf(host.split(":")[1]))
        } else {
            return null
        }
    }

    fun hasPrimary(): Boolean {
        return getPrimary() != null;
    }

    fun waitForPrimary(): Mongod? {
        Awaitility.await()
            .atMost(30, TimeUnit.SECONDS)
            .until({ while (!hasPrimary()) { Thread.sleep(500) } })

        return getPrimary()
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
        client?.close()
        client = null;
        for (node in nodes) {
            node.shutdown()
        }
    }
}