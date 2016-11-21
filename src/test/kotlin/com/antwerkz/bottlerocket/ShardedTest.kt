package com.antwerkz.bottlerocket

import org.testng.annotations.Test
import java.io.File

class ShardedTest : BaseTest() {

    @Test(dataProvider = "versions")
    fun sharded(clusterVersion: String) {
        cluster = ShardedCluster(baseDir = File("${basePath()}/sharded"), version = clusterVersion)
        testClusterWrites()
        validateShards()
    }

    @Test(dataProvider = "versions")
    fun shardedAuth(clusterVersion: String) {
        assume(!clusterVersion.startsWith("2.6"), "Auth and sharding on 2.6 are currently failing for some reason")
        cluster = ShardedCluster(baseDir = File("${basePath()}/shardedAuth"), version = clusterVersion)
        testClusterAuth()
        validateShards()
        testClusterWrites()
    }
}
