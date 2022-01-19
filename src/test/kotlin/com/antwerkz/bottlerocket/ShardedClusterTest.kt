package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.BottleRocket.DEFAULT_VERSION
import com.antwerkz.bottlerocket.clusters.ShardedCluster
import com.github.zafarkhaja.semver.Version
import org.testng.annotations.Test
import java.io.File

class ShardedClusterTest : BaseTest() {
    @Test(dataProvider = "versions", enabled = false)
    fun sharded(version: Version) {
        ShardedCluster(baseDir = File("${basePath(DEFAULT_VERSION)}/sharded"), version = version).use {
            startCluster(it)
            testClusterWrites(it)
            validateShards(it)
        }
    }

    @Test(dataProvider = "versions", enabled = false)
    fun shardedAuth(version: Version) {
        ShardedCluster(baseDir = File("${basePath(version)}/shardedAuth"), version = version).use {
            startCluster(it)
            testClusterAuth(it)
            validateShards(it)
            testClusterWrites(it)
        }
    }
}
