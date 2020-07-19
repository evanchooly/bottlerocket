package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.clusters.ReplicaSet
import com.github.zafarkhaja.semver.Version
import org.testng.annotations.Test
import java.io.File

class ReplicaSetTest : BaseTest() {
    @Test(dataProvider = "versions")
    fun replicaSet(version: Version) {
        cluster = ReplicaSet(baseDir = File("${basePath(version)}/replicaSet").absoluteFile, version = version)
        testClusterWrites()
        assertPrimary(30000)
    }

    @Test(dataProvider = "versions", enabled = false)
    fun replicaSetAuth(version: Version) {
        cluster = ReplicaSet(baseDir = File("${basePath(version)}/replicaSetAuth").absoluteFile, version = version)
        testClusterAuth()
        testClusterWrites()
        assertPrimary(30000)
    }
}
