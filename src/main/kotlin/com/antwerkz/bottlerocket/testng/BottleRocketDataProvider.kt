package com.antwerkz.bottlerocket.testng

import com.antwerkz.bottlerocket.MongoCluster
import com.antwerkz.bottlerocket.SingleNode
import com.antwerkz.bottlerocket.SingleNodeBuilder
import org.testng.annotations.DataProvider

public class BottleRocketDataProvider {
    DataProvider(name = "bottlerocket")
    fun cluster(): Array<Array<MongoCluster>> {
        val clusters = arrayListOf<MongoCluster>()

        System.getProperty("rocket.cluster.versions", "installed").split('.').forEach { clusterVersion ->
            System.getProperty("rocket.cluster.types", "single").split('.').forEach { type ->
                when(type) {
                    "single" -> { clusters.add(SingleNode.builder {
                        version = clusterVersion
                    })}
                    "replicaSet" -> {}
                    "sharded" -> {}
                }
            }

        }

        return clusters.map { arrayOf(it) }.toTypedArray()
    }
}