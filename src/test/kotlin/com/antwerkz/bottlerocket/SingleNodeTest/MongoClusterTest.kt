package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.configuration.mongo30.Configuration
import com.antwerkz.bottlerocket.configuration.mongo30.configuration
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File
import java.lang

class SingleNodeTest : BaseTest() {

    @Test(dataProvider = "versions")
    public fun singleNode(clusterVersion: String) {
        cluster = SingleNode(baseDir = File("build/rocket/singleNode").getAbsoluteFile(), version = clusterVersion)
        testClusterWrites()
    }

//    @Test(dataProvider = "versions")
    fun singleNodeAuth(clusterVersion: String) {
        cluster = SingleNode(baseDir = File("build/rocket/singleNodeAuth").getAbsoluteFile(), version = clusterVersion)
        testClusterAuth()
        testClusterWrites()
    }

    @DataProvider(name = "versions")
    fun configClusters(): Array<Array<String>> {
        return arrayOf(
//              arrayOf("3.0.5"),
              arrayOf("2.6.10")//,
//              arrayOf("2.4.14")
        )
    };
}
