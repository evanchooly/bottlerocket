package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.clusters.MongoCluster
import com.antwerkz.bottlerocket.clusters.ReplicaSet
import org.bson.Document
import org.testng.Assert
import org.testng.SkipException
import org.testng.annotations.AfterMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.ArrayList

open class BaseTest {
    companion object {
        val versions = arrayOf(
                arrayOf("3.2.11"),
                arrayOf("3.0.14"),
                arrayOf("2.6.12")
        )
    }

    lateinit var cluster: MongoCluster

    @AfterMethod
    fun stopCluster() {
        cluster.shutdown()
    }

    @DataProvider(name = "versions")
    fun versions(): Array<Array<String>> {
        return BaseTest.versions
    }

    fun testClusterWrites() {
        startCluster()

        val client = cluster.getClient()
        val names = client.listDatabaseNames()?.into(ArrayList<String>())
        Assert.assertFalse(names?.isEmpty() ?: true, names.toString())

        val db = client.getDatabase("rockettest")
        db?.drop()
        val collection = db?.getCollection("singlenode")
        val document = Document(hashMapOf<String, Any>("key" to "value"))
        collection?.insertOne(document)

        Assert.assertEquals(collection?.find()?.first(), document)
    }

    private fun startCluster(enableAuth: Boolean = false) {
        if (!cluster.isStarted()) {
            cluster.clean()
            cluster.start()
            if (false && enableAuth) {
                cluster.addUser("rockettest", "rocket", "cluster",
                        listOf(DatabaseRole("readWrite"), DatabaseRole("clusterAdmin", "admin"), DatabaseRole("dbAdmin")))

                cluster.shutdown()
                cluster.enableAuth()
                cluster.start()
            }
        }
    }

    fun testClusterAuth() {
        startCluster(true)

        Assert.assertTrue(cluster.isAuthEnabled())

        val client = cluster.getClient()

        val names = client.listDatabaseNames().into(ArrayList<String>())
        Assert.assertFalse(names.isEmpty(), names.toString())
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
        val list = cluster.getAdminClient().getDatabase("config")?.getCollection("shards")?.find()?.into(ArrayList<Document>())
        Assert.assertEquals(list?.size, 1, "Should find 1 shards")
        for (document in list ?: listOf<Document>()) {
            when (document.getString("_id")) {
                "rocket0" -> Assert.assertEquals(document["host"], "rocket0/localhost:30001,localhost:30002,localhost:30003")
                else -> Assert.fail("found unknown shard member: " + document)
            }
        }
    }

    fun assume(condition: Boolean, message: String) {
        if (!condition) {
            throw SkipException(message)
        }
    }

    protected fun basePath(): String {
        val format = LocalTime.now().format(DateTimeFormatter.ofPattern("hhmmss"))
        return "build/rocket/$format/"
    }
}