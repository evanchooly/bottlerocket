package com.antwerkz.bottlerocket.testng

import com.antwerkz.bottlerocket.MongoCluster
import com.antwerkz.bottlerocket.ReplicaSet
import com.antwerkz.bottlerocket.ShardedCluster
import com.antwerkz.bottlerocket.SingleNode
import org.testng.Assert
import org.testng.annotations.Test
import kotlin.reflect.KClass
import kotlin.reflect.jvm.kotlin

Test
class BottleRocketDataProviderTest {
    fun clusterTypes() {
        val provider = BottleRocketDataProvider()
        check(provider, "single", SingleNode::class)
        check(provider, "replicaSet", ReplicaSet::class)
        check(provider, "sharded", ShardedCluster::class)
        check(provider, "asl;dkfjals;dkfjas", SingleNode::class)
        check(provider, null, SingleNode::class)
    }

    private fun check<T: MongoCluster>(provider: BottleRocketDataProvider, value: String?, type: KClass<T>) {
        if (value != null) {
            System.setProperty(BottleRocketDataProvider.CLUSTER_TYPES, value)
        } else {
            System.getProperties().remove(BottleRocketDataProvider.CLUSTER_TYPES)
        }
        Assert.assertEquals(provider.cluster()[0][0].javaClass.kotlin, type, "Expected a ${type} for the value '${value}");
    }
}