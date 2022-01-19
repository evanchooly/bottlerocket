package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.clusters.SingleNode
import com.github.zafarkhaja.semver.Version
import org.testng.annotations.Test
import java.io.File

class SingleNodeTest : BaseTest() {
    @Test(dataProvider = "versions")
    fun singleNode(version: Version) {
        SingleNode(clusterRoot = File("${basePath(version)}/singleNode"), version = version).use {
            startCluster(it)
            testClusterWrites(it)
        }
    }

    @Test(dataProvider = "versions", enabled = false)
    fun singleNodeAuth(version: Version) {
        SingleNode(clusterRoot = File("${basePath(version)}/singleNodeAuth"), version = version).use {
            startCluster(it)
            testClusterAuth(it)
            testClusterWrites(it)
        }
    }
}
