package com.antwerkz.bottlerocket.clusters

import com.antwerkz.bottlerocket.ReplicaSet

class ReplicaSetBuilder(): MongoClusterBuilder<ReplicaSetBuilder>() {
    var size: Int = 3
        private set

    fun size(value: Int) : ReplicaSetBuilder {
        size = value;
        return this;
    }

    fun build(): ReplicaSet {
        return ReplicaSet(name, port, version, baseDir, size)
    }
}