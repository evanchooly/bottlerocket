package com.antwerkz.bottlerocket

import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class ShardedTest : BaseTest() {

    @Test(dataProvider = "versions")
    public fun sharded(clusterVersion: String) {
        cluster = ShardedCluster(baseDir = File("build/rocket/sharded"), version = clusterVersion)
        testClusterWrites()
        validateShards()
    }

    //    @Test(dataProvider = "versions")
    fun shardedAuth(clusterVersion: String) {
        cluster = ShardedCluster(baseDir = File("build/rocket/shardedAuth"), version = clusterVersion)
        testClusterAuth()
        validateShards()
        testClusterWrites()
    }

    @DataProvider(name = "versions")
    fun versions(): Array<Array<String>> {
        return BaseTest.versions
    };
}
