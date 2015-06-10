package com.antwerkz.bottlerocket.testng

import com.antwerkz.bottlerocket.SingleNode
import org.testng.Assert
import org.testng.annotations.Test

Test
class BottleRocketDataProviderTest {
    fun singleInstalled() {
        val clusters = BottleRocketDataProvider().cluster()
        Assert.assertEquals(1, clusters.size())
        val mongoCluster = clusters[0][0]
        Assert.assertTrue(mongoCluster is SingleNode)
        Assert.assertEquals("installed", mongoCluster.version)
        Assert.assertEquals(30000, mongoCluster.port)
    }
}