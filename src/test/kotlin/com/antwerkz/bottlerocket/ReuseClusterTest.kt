package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.clusters.MongoCluster
import com.antwerkz.bottlerocket.clusters.ReplicaSet
import com.antwerkz.bottlerocket.clusters.ShardedCluster
import com.antwerkz.bottlerocket.clusters.SingleNode
import org.bson.Document
import org.testng.Assert
import org.testng.annotations.AfterMethod
import org.testng.annotations.Test
import java.io.File

@Test(enabled = false)
class ReuseClusterTest {
    var cluster: MongoCluster? = null
    val rootDir = File("target/reuse")

    @AfterMethod
    fun stop() {
        cluster?.shutdown()
    }

    fun reuseSingleNode() {
        reuseDirectory {
            SingleNode(rootDir)
        }
    }

    fun reuseReplicaSet() {
        reuseDirectory {
            ReplicaSet(rootDir)
        }
    }

    fun reuseShardedCluster() {
        reuseDirectory {
            ShardedCluster(rootDir)
        }
    }

    private fun reuseDirectory(create: () -> MongoCluster) {
        rootDir.deleteRecursively()
        cluster = create()

        cluster?.start()
        var client = cluster?.getClient() ?: throw RuntimeException("Should have found a client")
        var collection = client.getDatabase("reuse")
            .getCollection("testing")

        collection.insertOne(Document("key", "value"))

        Assert.assertEquals(1, collection.countDocuments())

        cluster?.shutdown()

        cluster = create()

        cluster?.start()
        client = cluster?.getClient() ?: throw RuntimeException("Should have found a client")
        collection = client.getDatabase("reuse")
            .getCollection("testing")

        Assert.assertEquals(1, collection.countDocuments())
    }
}