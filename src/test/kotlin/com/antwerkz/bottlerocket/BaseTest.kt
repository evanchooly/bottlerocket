package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.configuration.mongo30.configuration
import com.jayway.awaitility.Awaitility
import org.bson.Document
import org.slf4j.LoggerFactory
import org.testng.Assert
import org.testng.annotations.AfterMethod
import java.time.Duration
import java.time.LocalDateTime
import java.util.ArrayList
import java.util.concurrent.TimeUnit

open class BaseTest {
    companion object {
        private val LOG = LoggerFactory.getLogger(javaClass<BaseTest>())
        val versions = arrayOf(
              arrayOf("3.1.6"),
              arrayOf("3.0.5"),
              arrayOf("2.6.10"),
              arrayOf("2.4.14")//,
//              arrayOf("2.2.7")
        )
    }

    var cluster: MongoCluster? = null

    AfterMethod
    fun sleep() {
        cluster?.shutdown()
        LOG.info("Sleeping between tests")
        Thread.sleep(1000)
    }

    fun testClusterWrites() {
        startCluster()

        val client = cluster?.getClient()
        val names = client?.listDatabaseNames()?.into(ArrayList<String>())
        Assert.assertFalse(names?.isEmpty() ?: true, names.toString())

        val db = client?.getDatabase("rockettest")
        db?.drop()
        val collection = db?.getCollection("singlenode")
        val document = Document(hashMapOf("key" to "value"))
        collection?.insertOne(document)

        Assert.assertEquals(collection?.find()?.first(), document)
    }

    private fun startCluster(enableAuth: Boolean = false) {
        if (cluster != null && !cluster!!.isStarted() ) {
            cluster?.clean()
            if(enableAuth) {
                cluster?.startWithAuth()
                cluster?.addUser("rockettest", "rocket", "cluster",
                      listOf(DatabaseRole("readWrite"), DatabaseRole("clusterAdmin", "admin"), DatabaseRole("dbAdmin")));
            } else {
                cluster?.start()
            }

            var allActive = false
            val start = LocalDateTime.now();

            // TODO:  replace with awaitility
            val timeout = 30000
            while (!allActive && Duration.between(start, LocalDateTime.now()).toMillis() < timeout) {
                try {
                    cluster?.allNodesActive()
                    allActive = true
                } catch(e: IllegalStateException) {
                    if (Duration.between(start, LocalDateTime.now()).toMillis() > timeout) {
                        throw e
                    }
                }
                Thread.sleep(1000)
            }

            if(!allActive) {
                throw IllegalStateException("Not all cluster members are active");
            }
        }
    }

    fun testClusterAuth() {
        startCluster(true)

        Assert.assertTrue(cluster?.isAuthEnabled() ?: false)

        var client = cluster?.getClient()

        val names = client?.listDatabaseNames()?.into(ArrayList<String>())
        Assert.assertFalse(names?.isEmpty() ?: true, names.toString())
    }

    fun assertPrimary(port: Int) {
        if (cluster is ReplicaSet) {
            val replicaSet = cluster as ReplicaSet
            Assert.assertTrue(replicaSet.hasPrimary())
            Assert.assertTrue(replicaSet.waitForPrimary() != null)
            val primary = replicaSet.getPrimary()
            Assert.assertEquals(primary?.port, port, "${port} should be the primary at startup")
        } else {
            Assert.fail("${cluster} is not a replica set cluster")
        }
    }

    fun validateShards() {
        val list = cluster?.getAdminClient()?.getDatabase("config")?.getCollection("shards")?.find()?.into(ArrayList<Document>())
        Assert.assertEquals(list?.size(), 1, "Should find 1 shards")
        for (document in list ?: listOf<Document>()) {
            when (document.getString("_id")) {
                "rocket0" -> Assert.assertEquals(document["host"], "rocket0/localhost:30001,localhost:30002,localhost:30003")
                else -> Assert.fail("found unknown shard member: " + document)
            }
        }
    }

}