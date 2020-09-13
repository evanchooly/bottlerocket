package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.clusters.ReplicaSet
import com.github.zafarkhaja.semver.Version
import org.testng.Assert
import org.testng.annotations.Test
import java.io.File

class ReplicaSetTest : BaseTest() {
    @Test(dataProvider = "versions")
    fun replicaSet(version: Version) {
        cluster = ReplicaSet(clusterRoot = File("${basePath(version)}/replicaSet"), version = version, allocator = portAllocator)
        testClusterWrites()
        assertHasPrimary(30000)
    }

    @Test(dataProvider = "versions", enabled = false)
    fun replicaSetAuth(version: Version) {
        cluster = ReplicaSet(clusterRoot = File("${basePath(version)}/replicaSetAuth"), version = version,
            allocator = portAllocator)
        testClusterAuth()
        testClusterWrites()
        assertHasPrimary(30000)
    }

    fun assertHasPrimary(port: Int) {
        val replicaSet = cluster as ReplicaSet
        Assert.assertTrue(replicaSet.waitForPrimary() != null)
    }
}
