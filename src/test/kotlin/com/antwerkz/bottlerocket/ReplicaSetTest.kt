package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.clusters.ReplicaSet
import com.github.zafarkhaja.semver.Version
import org.testng.Assert
import org.testng.annotations.Test
import java.io.File

class ReplicaSetTest : BaseTest() {
    @Test(dataProvider = "versions")
    fun replicaSet(version: Version) {
        ReplicaSet(version = version, clusterRoot = File("${basePath(version)}/replicaSet")).use {
            startCluster(it)
            testClusterWrites(it)
            assertHasPrimary(it)
        }
    }

    @Test(dataProvider = "versions", enabled = false)
    fun replicaSetAuth(version: Version) {
        ReplicaSet(version = version, clusterRoot = File("${basePath(version)}/replicaSetAuth")).use {
            startCluster(it)
            testClusterAuth(it)
            testClusterWrites(it)
            assertHasPrimary(it)
        }
    }

    fun assertHasPrimary(replicaSet: ReplicaSet) {
        Assert.assertTrue(replicaSet.waitForPrimary() != null)
    }
}
