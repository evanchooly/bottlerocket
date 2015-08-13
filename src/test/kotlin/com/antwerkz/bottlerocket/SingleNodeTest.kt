package com.antwerkz.bottlerocket

import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class SingleNodeTest : BaseTest() {
    @Test(dataProvider = "versions")
    public fun singleNode(clusterVersion: String) {
        cluster = SingleNode(baseDir = File("build/rocket/singleNode").getAbsoluteFile(), version = clusterVersion)
        testClusterWrites()
    }

    @Test(dataProvider = "versions")
    fun singleNodeAuth(clusterVersion: String) {
        cluster = SingleNode(baseDir = File("build/rocket/singleNodeAuth").getAbsoluteFile(), version = clusterVersion)
        testClusterAuth()
        testClusterWrites()
    }

    @DataProvider(name = "versions")
    fun versions(): Array<Array<String>> {
        return BaseTest.versions
    };
}
