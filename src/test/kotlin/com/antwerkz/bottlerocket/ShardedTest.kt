package com.antwerkz.bottlerocket

import com.github.zafarkhaja.semver.Version
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

    @Test(dataProvider = "versions")
    fun shardedAuth(clusterVersion: String) {
        cluster = ShardedCluster(baseDir = File("build/rocket/shardedAuth"), version = clusterVersion)
        assume(cluster!!.versionAtLeast(Version.valueOf("2.6.0")), "Authentication not currently supported prior to version 2.6")
        testClusterAuth()
        validateShards()
        testClusterWrites()
    }

/*
    @DataProvider(name = "versions")
    fun versions(): Array<Array<String>> {
        return BaseTest.versions
    };
*/
}
