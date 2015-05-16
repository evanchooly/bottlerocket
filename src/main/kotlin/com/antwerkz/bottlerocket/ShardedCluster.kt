package com.antwerkz.bottlerocket

import java.io.File

class ShardedCluster(name: String = DEFAULT_MONGOD_NAME, port: Int,
                    version: String, public var size: Int): MongoCluster() {
    var shards: MutableList<ReplicaSet> = arrayListOf()
    var configServers: MutableList<ConfigServer> = arrayListOf()

    override
    fun start() {
/*
        if (shards.isEmpty()) {
            var port = basePort
            for ( i in 0..size - 1) {
                val replSetName = "${name}${i}"
                val replicaSet = MongoCluster.replicaSet(name, port, version)
                replicaSet.logDir = File(logDir, replSetName)
                replicaSet.dataDir = File(dataDir, replSetName)
                shards.add(replicaSet)
                port += replicaSet.size;
            }
        }
*/
    }

    override
    fun shutdown() {
    }

    override
    fun clean() {

    }
}