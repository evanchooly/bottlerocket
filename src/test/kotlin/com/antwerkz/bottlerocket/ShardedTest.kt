package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.clusters.ShardedCluster
import com.github.zafarkhaja.semver.Version
import org.testng.annotations.Test
import java.io.File

class ShardedTest : BaseTest() {

    @Test(dataProvider = "versions", enabled = false)
    fun sharded(version: Version) {
        cluster = ShardedCluster(baseDir = File("${basePath(version)}/sharded"), version = version)
        testClusterWrites()
        validateShards()
    }

    @Test(dataProvider = "versions", enabled = false)
    fun shardedAuth(version: Version) {
        cluster = ShardedCluster(baseDir = File("${basePath(version)}/shardedAuth"), version = version)
        testClusterAuth()
        validateShards()
        testClusterWrites()
    }
}
