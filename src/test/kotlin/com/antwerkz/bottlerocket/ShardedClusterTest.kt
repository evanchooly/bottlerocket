package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.clusters.ShardedCluster
import com.github.zafarkhaja.semver.Version
import org.testng.annotations.Test
import java.io.File

class ShardedClusterTest : BaseTest() {
    @Test(dataProvider = "versions")
    fun sharded(version: Version) {
        ShardedCluster(version = version, baseDir = File("${basePath(version)}/sharded"))
            .use {
                startCluster(it)
                testClusterWrites(it)
                validateShards(it)
            }
    }

    @Test(dataProvider = "versions", enabled = false)
    fun shardedAuth(version: Version) {
        ShardedCluster(version = version, baseDir = File("${basePath(version)}/shardedAuth")).use {
            startCluster(it)
            testClusterAuth(it)
            validateShards(it)
            testClusterWrites(it)
        }
    }
}
