package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.clusters.MongoCluster
import com.antwerkz.bottlerocket.clusters.ReplicaSet
import com.antwerkz.bottlerocket.clusters.ShardedCluster
import com.antwerkz.bottlerocket.clusters.SingleNode
import org.bson.Document
import org.testng.Assert
import org.testng.annotations.Test
import java.io.File

@Test(enabled = false)
class ReuseClusterTest: BaseTest() {
    val rootDir = File("target/reuse")

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
        var cluster = create()

        cluster.start()
        var client = cluster.getClient()
        var collection = client.getDatabase("reuse")
            .getCollection("testing")

        collection.insertOne(Document("key", "value"))

        Assert.assertEquals(1, collection.countDocuments())

        cluster.shutdown()

        cluster = create()

        cluster.start()
        client = cluster.getClient()
        collection = client.getDatabase("reuse")
            .getCollection("testing")

        Assert.assertEquals(1, collection.countDocuments())
    }
}