package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.clusters.ReplicaSet
import com.github.zafarkhaja.semver.Version
import org.testng.annotations.DataProvider
import org.testng.annotations.Listeners
import org.testng.annotations.Test
import java.io.File

class ReplicaSetTest : BaseTest() {
    @Test(dataProvider = "versions")
    fun replicaSet(clusterVersion: Version) {
        cluster = ReplicaSet(baseDir = File("${basePath()}/replicaSet").absoluteFile, version = clusterVersion)
        testClusterWrites()
        assertPrimary(30000)
    }

    @Test(dataProvider = "versions", enabled = false)
    fun replicaSetAuth(clusterVersion: Version) {
        cluster = ReplicaSet(baseDir = File("${basePath()}/replicaSetAuth").absoluteFile, version = clusterVersion)
        testClusterAuth()
        testClusterWrites()
        assertPrimary(30000)
    }
}
