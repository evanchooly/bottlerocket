package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.clusters.SingleNode
import com.github.zafarkhaja.semver.Version
import org.testng.annotations.Test
import java.io.File

class SingleNodeTest : BaseTest() {
    @Test(dataProvider = "versions")
    fun singleNode(version: Version) {
        cluster = SingleNode(clusterRoot = File("${basePath(version)}/singleNode").absoluteFile, version = version)
        testClusterWrites()
    }

    @Test(dataProvider = "versions", enabled = false)
    fun singleNodeAuth(version: Version) {
        cluster = SingleNode(clusterRoot = File("${basePath(version)}/singleNodeAuth").absoluteFile, version = version)
        testClusterAuth()
        testClusterWrites()
    }
}
