package com.antwerkz.bottlerocket

import com.github.zafarkhaja.semver.Version
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class SingleNodeTest : BaseTest() {
    @Test(dataProvider = "versions")
    public fun singleNode(clusterVersion: String) {
        cluster = SingleNode(baseDir = File("build/rocket/singleNode").absoluteFile,
                version = clusterVersion)
        testClusterWrites()
    }

    @Test(dataProvider = "versions")
    fun singleNodeAuth(clusterVersion: String) {
        cluster = SingleNode(baseDir = File("build/rocket/singleNodeAuth").absoluteFile,
                version = clusterVersion)
        testClusterAuth()
        testClusterWrites()
    }
}
