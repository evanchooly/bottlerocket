package com.antwerkz.bottlerocket.testng

import com.antwerkz.bottlerocket.clusters.MongoCluster
import com.antwerkz.bottlerocket.BottleRocket.DEFAULT_VERSION
import com.antwerkz.bottlerocket.clusters.ReplicaSet
import com.antwerkz.bottlerocket.clusters.ShardedCluster
import com.antwerkz.bottlerocket.clusters.SingleNode
import org.testng.annotations.DataProvider

class BottleRocketDataProvider {
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