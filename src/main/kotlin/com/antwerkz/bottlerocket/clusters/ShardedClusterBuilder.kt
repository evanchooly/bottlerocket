package com.antwerkz.bottlerocket.clusters

import com.antwerkz.bottlerocket.ShardedCluster

class ShardedClusterBuilder() : MongoClusterBuilder<ShardedClusterBuilder>() {
    var shardCount: Int = 1;
        private set
    var mongosCount: Int = 1;
        private set
    var configSvrCount: Int = 1;
        private set

    fun shardCount(value: Int) : ShardedClusterBuilder {
        shardCount = value;
        return this;
    }

    fun mongosCount(value: Int) : ShardedClusterBuilder {
        mongosCount = value;
        return this;
    }

    fun configSvrCount(value: Int) : ShardedClusterBuilder {
        configSvrCount = value;
        return this;
    }

    fun build(): ShardedCluster {
        return ShardedCluster(name, port, version, baseDir, shardCount, mongosCount, configSvrCount)
    }
}