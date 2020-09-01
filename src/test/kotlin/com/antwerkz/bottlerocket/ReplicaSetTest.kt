package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.BottleRocket.DEFAULT_VERSION
import com.antwerkz.bottlerocket.clusters.ReplicaSet
import com.github.zafarkhaja.semver.Version
import org.testng.annotations.Test
import java.io.File

class ReplicaSetTest : BaseTest() {
    @Test // (dataProvider = "versions")
    fun replicaSet() {
        cluster = ReplicaSet(clusterRoot = File("${basePath(DEFAULT_VERSION)}/replicaSet"), allocator = portAllocator)
        testClusterWrites()
        assertPrimary(30000)
    }

    @Test(dataProvider = "versions", enabled = false)
    fun replicaSetAuth(version: Version) {
        cluster = ReplicaSet(clusterRoot = File("${basePath(version)}/replicaSetAuth"), version = version,
            allocator = portAllocator)
        testClusterAuth()
        testClusterWrites()
        assertPrimary(30000)
    }
}
