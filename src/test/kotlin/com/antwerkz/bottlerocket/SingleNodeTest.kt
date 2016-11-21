package com.antwerkz.bottlerocket

import org.testng.annotations.Test
import java.io.File

class SingleNodeTest : BaseTest() {
    @Test(dataProvider = "versions")
    fun singleNode(clusterVersion: String) {
        cluster = SingleNode(baseDir = File("${basePath()}/singleNode").absoluteFile, version = clusterVersion)
        testClusterWrites()
    }

    @Test(dataProvider = "versions")
    fun singleNodeAuth(clusterVersion: String) {
        cluster = SingleNode(baseDir = File("${basePath()}/singleNodeAuth").absoluteFile, version = clusterVersion)
        testClusterAuth()
        testClusterWrites()
    }
}
