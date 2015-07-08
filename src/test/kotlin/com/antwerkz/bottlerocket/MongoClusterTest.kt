package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.mongodb.MongoClient
import com.mongodb.WriteConcern
import org.bson.Document
import org.slf4j.LoggerFactory
import org.testng.Assert
import org.testng.annotations.AfterMethod
import org.testng.annotations.AfterTest
import org.testng.annotations.Test
import java.io.File
import java.util.ArrayList

class MongoClusterTest {
    companion object {
        private val LOG = LoggerFactory.getLogger(javaClass<MongoClusterTest>())
    }

    AfterMethod
    fun sleep() {
        LOG.info("Sleeping between tests")
        Thread.sleep(1000)
    }

    Test
    public fun singleNode() {
        val singleNode = SingleNode(baseDir = File("build/rocket/singleNode"))
        try {
            singleNode.clean()
            singleNode.start()

            val client = singleNode.getClient()
            val names = client.listDatabaseNames().into(ArrayList<String>())
            Assert.assertFalse(names.isEmpty(), names.toString())

            val db = client.getDatabase("bottlerocket")
            db.drop()
            val collection = db.getCollection("singlenode")
            val document = Document(hashMapOf("key" to "value"))
            collection.insertOne(document)

            Assert.assertEquals(collection.find().first(), document)
        } finally {
            singleNode.shutdown()
        }
    }

    Test
    fun singleNodeAuth() {
        testClusterAuth(SingleNode(baseDir = File("build/rocket/singleNodeAuth")), {})
    }

    Test
    public fun replicaSet() {
        val replicaSet = ReplicaSet(baseDir = File("build/rocket/replicaSet"))
        var client: MongoClient? = null
        try {
            replicaSet.clean()
            replicaSet.start()

            val primary = replicaSet.getPrimary()
            Assert.assertEquals(primary?.port, 30000, "3000 should be the primary at startup")
            client = replicaSet.getClient()
            val collection = client.getDatabase("bottlerocket").getCollection("replication")
            val document = Document("key", "value")

            collection.withWriteConcern(WriteConcern.ACKNOWLEDGED).insertOne(document)

            val first = collection.find().first()
            Assert.assertEquals(document, first)

            Assert.assertTrue(replicaSet.hasPrimary())

            Assert.assertTrue(replicaSet.waitForPrimary() != null)
        } finally {
            if (client != null) {
                client.close()
            }
            replicaSet.shutdown()
        }
    }

    Test
    fun replicaSetAuth() {
        testClusterAuth(ReplicaSet(baseDir = File("build/rocket/replicaSetAuth")), {})
    }

    Test
    public fun sharded() {
        val sharded = ShardedCluster(baseDir = File("build/rocket/sharded"))
        try {
            sharded.clean()
            sharded.start()
            validateShards(sharded)
        } finally {
            sharded.shutdown()
        }
    }

    Test
    fun shardedAuth() {
        val cluster = ShardedCluster(baseDir = File("build/rocket/shardedAuth"))
        testClusterAuth(cluster, { validateShards(cluster) })
    }

    private fun validateShards(cluster: MongoCluster) {
        val list = cluster.getClient().getDatabase("config").getCollection("shards").find().into(ArrayList<Document>())
        Assert.assertEquals(list.size(), 1, "Should find 1 shards")
        for (document in list) {
            when (document.getString("_id")) {
                "rocket0" -> Assert.assertEquals(document["host"], "rocket0/localhost:30001,localhost:30002,localhost:30003")
                else -> Assert.fail("found unknown shard member: " + document)
            }
        }
    }

    private fun testClusterAuth(cluster: MongoCluster, test: () -> Unit) {
        var client: MongoClient? = null
        try {
            cluster.clean()
            cluster.enableAuth();
//            cluster.start();

            Assert.assertTrue(cluster.isAuthEnabled())

            client = cluster.getClient()

            val names = client.listDatabaseNames().into(ArrayList<String>())
            Assert.assertFalse(names.isEmpty(), names.toString())

            val db = client.getDatabase("bottlerocket")
            db.drop()
            val collection = db.getCollection("auth_test")
            val document = Document(hashMapOf("key" to "value"))
            collection.insertOne(document)

            Assert.assertEquals(collection.find().first(), document)

            test()
        } finally {
            client?.close()
            cluster.shutdown()
        }
    }
}