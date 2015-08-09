package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.configuration.mongo30.Configuration
import com.antwerkz.bottlerocket.configuration.mongo30.configuration
import org.testng.annotations.DataProvider
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

    @DataProvider(name = "clusters")
    fun configClusters(): Array<Array<Any>> {
        var config = configuration {
            net {
                http {
                    JSONPEnabled = true
                }
            }
        }

        return arrayOf(
              arrayOf(SingleNode(baseDir = File("build/rocket/single-fullConfig").getAbsoluteFile()), config),
              arrayOf(ReplicaSet(baseDir = File("build/rocket/replicaset-fullConfig").getAbsoluteFile()), config),
              arrayOf(ShardedCluster(baseDir = File("build/rocket/sharded-fullConfig").getAbsoluteFile()), config)
        )
    };

    @Test(dataProvider = "clusters")
    fun fullConfig(cluster: MongoCluster, config: Configuration) {
        cluster.clean()
        cluster.updateConfig(config);
        try {
            cluster.start();
        } finally {
            cluster.shutdown();
            Thread.sleep(1000);
        }
    }
}
