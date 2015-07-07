package com.antwerkz.bottlerocket.testng

import com.antwerkz.bottlerocket.MongoCluster
import com.antwerkz.bottlerocket.ReplicaSet
import com.antwerkz.bottlerocket.ShardedCluster
import com.antwerkz.bottlerocket.SingleNode
import com.antwerkz.bottlerocket.clusters.SingleNodeBuilder
import org.testng.annotations.DataProvider

public class BottleRocketDataProvider {
    DataProvider(name = "bottlerocket")
    fun cluster(): Array<Array<MongoCluster>> {
        val clusters = arrayListOf<MongoCluster>()

        System.getProperty("rocket.cluster.versions", "installed").split('.').forEach { clusterVersion ->
            System.getProperty("rocket.cluster.types", "single").split('.').forEach { type ->
                when(type) {
                    "single" -> { clusters.add(SingleNode(version = clusterVersion)) }
                    "replicaSet" -> {clusters.add(ReplicaSet(version = clusterVersion))}
                    "sharded" -> {clusters.add(ShardedCluster(version = clusterVersion))}
                }
            }

        }

        return clusters.map { arrayOf(it) }.toTypedArray()
    }
}