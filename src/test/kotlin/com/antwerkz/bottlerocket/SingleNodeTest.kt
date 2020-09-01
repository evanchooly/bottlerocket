package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.clusters.SingleNode
import com.github.zafarkhaja.semver.Version
import org.testng.annotations.Test
import java.io.File

class SingleNodeTest : BaseTest() {
    @Test(dataProvider = "versions")
    fun singleNode(version: Version) {
        if (version.greaterThanOrEqualTo(Version.forIntegers(4, 0, 0))) {
            cluster = SingleNode(clusterRoot = File("${basePath(version)}/singleNode"), version = version,
                port = portAllocator.next())
            testClusterWrites()
        }
    }

    @Test(dataProvider = "versions", enabled = false)
    fun singleNodeAuth(version: Version) {
        cluster = SingleNode(clusterRoot = File("${basePath(version)}/singleNodeAuth"), version = version)
        testClusterAuth()
        testClusterWrites()
    }
}
