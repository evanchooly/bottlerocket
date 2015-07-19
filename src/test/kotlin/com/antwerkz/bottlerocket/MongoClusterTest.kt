package com.antwerkz.bottlerocket

import org.testng.annotations.Test
import java.io.File

class MongoClusterTest : BaseTest() {

    Test
    public fun singleNode() {
        cluster = SingleNode(baseDir = File("build/rocket/singleNode"))
        testClusterWrites()
    }

    Test
    fun singleNodeAuth() {
        cluster = SingleNode(baseDir = File("build/rocket/singleNodeAuth"))
        testClusterAuth()
        testClusterWrites()
    }

    Test
    public fun replicaSet() {
        cluster = ReplicaSet(baseDir = File("build/rocket/replicaSet"))
        testClusterWrites()
        assertPrimary(30000)
    }

    Test
    fun replicaSetAuth() {
        cluster = ReplicaSet(baseDir = File("build/rocket/replicaSetAuth"))
        testClusterAuth()
        testClusterWrites()
    }

    Test
    public fun sharded() {
        cluster = ShardedCluster(baseDir = File("build/rocket/sharded"))
        testClusterWrites()
        validateShards()
    }

    Test
    fun shardedAuth() {
        cluster = ShardedCluster(baseDir = File("build/rocket/shardedAuth"))
        testClusterAuth()
        validateShards()
        testClusterWrites()
    }
}
