package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.clusters.ShardedCluster
import com.github.zafarkhaja.semver.Version
import org.testng.annotations.Test
import java.io.File

@Test(enabled = false)
class ShardedTest : BaseTest() {

    @Test(dataProvider = "versions")
    fun sharded(clusterVersion: Version) {
        cluster = ShardedCluster(baseDir = File("${basePath()}/sharded"), version = clusterVersion)
        testClusterWrites()
        validateShards()
    }

    @Test(dataProvider = "versions", enabled = false)
    fun shardedAuth(clusterVersion: Version) {
        cluster = ShardedCluster(baseDir = File("${basePath()}/shardedAuth"), version = clusterVersion)
        testClusterAuth()
        validateShards()
        testClusterWrites()
    }
}
