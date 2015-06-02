package com.antwerkz.bottlerocket

import com.mongodb.MongoClient
import com.mongodb.ServerAddress
import com.mongodb.WriteConcern
import org.bson.Document
import org.testng.Assert
import org.testng.annotations.Test
import java.net.UnknownHostException
import java.util.ArrayList

class MongoClusterTest {
    Test
    public fun singleNode() {
        val mongod = SingleNode.builder().build()
        val client = MongoClient("localhost", 30000)
        try {
            mongod.clean()
            mongod.start()

            val names = client.listDatabaseNames().into(ArrayList<String>())
            Assert.assertFalse(names.isEmpty(), names.toString())

            val db = client.getDatabase("bottlerocket")
            db.drop()
            val collection = db.getCollection("singlenode")
            val document = Document(hashMapOf("key" to "value"))
            collection.insertOne(document)

            Assert.assertEquals(collection.find().first(), document)
        } finally {
            client.close()
            mongod.shutdown()
        }
    }

    Test
    public fun replicaSet() {
        val replicaSet = ReplicaSet.builder().build()
        var client: MongoClient? = null
        try {
            replicaSet.clean()

            replicaSet.start()

            val primary = replicaSet.getPrimary()
            Assert.assertEquals(primary?.port, 30000, "30000 should be the primary at startup")
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
    public fun sharded() {
        val sharded = ShardedCluster.builder().build()
        var client: MongoClient? = null
        try {
            sharded.clean()
            sharded.start()
            client = MongoClient(listOf(ServerAddress("localhost", 30000), ServerAddress("localhost", 30001),
                  ServerAddress("localhost", 30002)))

            val list = client.getDatabase("config").getCollection("shards").find().into(ArrayList<Document>())
            Assert.assertEquals(list.size(), 3, "Should find 3 shards")
            for (document in list) {
                when (document.getString("_id")) {
                    "rocket0" -> Assert.assertEquals(document["host"], "rocket0/localhost:30003,localhost:30004,localhost:30005")
                    "rocket1" -> Assert.assertEquals(document["host"], "rocket1/localhost:30006,localhost:30007,localhost:30008")
                    "rocket2" -> Assert.assertEquals(document["host"], "rocket2/localhost:30009,localhost:30010,localhost:30011")
                    else -> Assert.fail("found unknown shard member: " + document)
                }
            }
        } finally {
            if (client != null) {
                client.close()
            }
            sharded.shutdown()
        }
    }
}