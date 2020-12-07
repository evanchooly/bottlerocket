package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.clusters.ReplicaSet
import com.github.zafarkhaja.semver.Version
import org.slf4j.LoggerFactory
import org.testng.Assert
import org.testng.annotations.Test
import java.io.File

class ReplicaSetTest : BaseTest() {
    @Test(dataProvider = "versions")
    fun replicaSet(version: Version) {
        if (version.greaterThanOrEqualTo(Version.forIntegers(4))) {
            ReplicaSet(clusterRoot = File("${basePath(version)}/replicaSet"), version = version).use {
                startCluster(it)
                testClusterWrites(it)
                assertHasPrimary(it)
            }
        }
    }

    @Test(dataProvider = "versions", enabled = false)
    fun replicaSetAuth(version: Version) {
        ReplicaSet(clusterRoot = File("${basePath(version)}/replicaSetAuth"), version = version).use {
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
