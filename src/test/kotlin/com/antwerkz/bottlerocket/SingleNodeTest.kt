package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.clusters.SingleNode
import com.github.zafarkhaja.semver.Version
import org.testng.annotations.Test
import java.io.File

class SingleNodeTest : BaseTest() {
    @Test(dataProvider = "versions")
    fun singleNode(clusterVersion: Version) {
        cluster = SingleNode(baseDir = File("${basePath()}/singleNode").absoluteFile, version = clusterVersion)
        testClusterWrites()
    }

    @Test(dataProvider = "versions", enabled = false)
    fun singleNodeAuth(clusterVersion: Version) {
        cluster = SingleNode(baseDir = File("${basePath()}/singleNodeAuth").absoluteFile, version = clusterVersion)
        testClusterAuth()
        testClusterWrites()
    }
}
