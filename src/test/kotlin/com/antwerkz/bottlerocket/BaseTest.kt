package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.clusters.MongoCluster
import com.github.zafarkhaja.semver.Version
import java.io.File
import java.util.ArrayList
import org.bson.Document
import org.testcontainers.shaded.org.apache.commons.lang3.SystemUtils
import org.testng.Assert
import org.testng.SkipException
import org.testng.annotations.DataProvider

open class BaseTest {
    @DataProvider(name = "versions")
    fun versions(): Array<Version> {
        return Versions.values().map { it.version() }.toTypedArray()
    }

    fun testClusterWrites(cluster: MongoCluster) {
        val client = cluster.getClient()
        val names = client.listDatabaseNames().into(ArrayList())
        Assert.assertFalse(names.isEmpty(), names.toString())
        val db = client.getDatabase("rockettest")
        db.drop()
        val collection = db.getCollection("singlenode")
        val document = Document(hashMapOf<String, Any>("key" to "value"))
        collection.insertOne(document)

        Assert.assertEquals(collection.find().first(), document)
    }

    fun startCluster(cluster: MongoCluster, enableAuth: Boolean = false) {
        if (!cluster.isStarted()) {
            cluster.clean()
            cluster.start()
            if (enableAuth) {
                cluster.addUser(
                    "rockettest",
                    "rocket",
                    "cluster",
                    listOf(
                        DatabaseRole("readWrite"),
                        DatabaseRole("clusterAdmin", "admin"),
                        DatabaseRole("dbAdmin")
                    )
                )

                cluster.shutdown()
                //                cluster.enableAuth()
                cluster.start()
            }
        }
    }

    fun testClusterAuth(cluster: MongoCluster) {
        startCluster(cluster, true)

        Assert.assertTrue(cluster.isAuthEnabled())
        val client = cluster.getClient()
        val names = client.listDatabaseNames().into(ArrayList())
        Assert.assertFalse(names.isEmpty(), names.toString())
    }

    fun validateShards(cluster: MongoCluster) {
        val list =
            cluster.adminClient
                .getDatabase("config")
                .getCollection("shards")
                .find()
                .into(ArrayList())
        Assert.assertEquals(list.size, 1, "Should find 1 shard")
    }

    protected fun basePath(version: Version): File {
        return File("target/rocket/$version")
    }

    protected fun assumeNotOldUbuntu(version: Version) {
        if (SystemUtils.IS_OS_LINUX) {
            val distro = LinuxDistribution.parse(File("/etc/os-release"))
            if (
                distro.name().equals("ubuntu", true) && version.lessThan(Version.valueOf("6.0.0"))
            ) {
                throw SkipException("$version not supported on Ubuntu")
            }
        }
    }
}
