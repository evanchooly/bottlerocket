package com.antwerkz.bottlerocket.clusters

import com.antwerkz.bottlerocket.BottleRocket
import com.antwerkz.bottlerocket.clusters.ClusterType.REPLICA_SET
import com.antwerkz.bottlerocket.clusters.ClusterType.SHARDED
import com.antwerkz.bottlerocket.clusters.ClusterType.SINGLE
import com.github.zafarkhaja.semver.Version
import java.io.File

class ClusterBuilder(var type: ClusterType) {
    private var version = BottleRocket.DEFAULT_VERSION
    private var name = BottleRocket.DEFAULT_NAME
    private var baseDir = BottleRocket.DEFAULT_BASE_DIR
    private var allocator = BottleRocket.PORTS

    fun version(value: Version): ClusterBuilder {
        version = value
        return this
    }

    fun name(value: String): ClusterBuilder {
        name = value
        return this
    }

    fun baseDir(value: File): ClusterBuilder {
        baseDir = value
        return this
    }

    fun allocator(value: PortAllocator): ClusterBuilder {
        allocator = value
        return this
    }

   fun <T: MongoCluster> build(): T {
        return when(type) {
            SINGLE -> SingleNode(version, name, baseDir, allocator.next())
            REPLICA_SET -> ReplicaSet(version, name, baseDir, allocator)
            SHARDED -> ShardedCluster(version, name, baseDir, allocator)
        } as T
    }
}

enum class ClusterType {
    SINGLE,
    REPLICA_SET,
    SHARDED
}