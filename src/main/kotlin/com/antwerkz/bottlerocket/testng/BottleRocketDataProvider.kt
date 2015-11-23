package com.antwerkz.bottlerocket.testng

import com.antwerkz.bottlerocket.MongoCluster
import com.antwerkz.bottlerocket.BottleRocket.DEFAULT_VERSION
import com.antwerkz.bottlerocket.ReplicaSet
import com.antwerkz.bottlerocket.ShardedCluster
import com.antwerkz.bottlerocket.SingleNode
import org.testng.annotations.DataProvider

public class BottleRocketDataProvider {
    companion object {
        val CLUSTER_VERSIONS = "rocket.cluster.versions"
        val CLUSTER_TYPES = "rocket.cluster.types"
    }

    @DataProvider(name = "bottlerocket")
    fun cluster(): Array<Array<MongoCluster>> {
        val clusterVersion = System.getProperty(CLUSTER_VERSIONS, DEFAULT_VERSION)
        val type = System.getProperty(CLUSTER_TYPES, "single")
        val cluster = when (type) {
            "single" -> SingleNode(version = clusterVersion)
            "replicaSet" -> ReplicaSet(version = clusterVersion)
            "sharded" -> ShardedCluster(version = clusterVersion)
            else -> SingleNode(version = clusterVersion)
        }

        return arrayOf(arrayOf(cluster))
    }
}