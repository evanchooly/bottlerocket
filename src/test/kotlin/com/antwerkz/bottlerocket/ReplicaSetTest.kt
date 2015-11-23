package com.antwerkz.bottlerocket

import com.github.zafarkhaja.semver.Version
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class ReplicaSetTest : BaseTest() {

    @Test(dataProvider = "versions")
    public fun replicaSet(clusterVersion: String) {
        cluster = ReplicaSet(baseDir = File("build/rocket/replicaSet"), version = clusterVersion)
        testClusterWrites()
        assertPrimary(30000)
    }

    @Test(dataProvider = "versions")
    fun replicaSetAuth(clusterVersion: String) {
        cluster = ReplicaSet(baseDir = File("build/rocket/replicaSetAuth").getAbsoluteFile(), version = clusterVersion)
        testClusterAuth()
        testClusterWrites()
        assertPrimary(30000)
    }
}
