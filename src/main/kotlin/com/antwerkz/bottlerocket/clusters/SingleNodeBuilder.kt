package com.antwerkz.bottlerocket.clusters

import com.antwerkz.bottlerocket.SingleNode

class SingleNodeBuilder() : MongoClusterBuilder<SingleNodeBuilder>() {
    constructor(mongod: SingleNode) : this() {
        name(mongod.name)
        port(mongod.port)
        version(mongod.version)
        baseDir(mongod.baseDir)
    }

    fun build(): SingleNode {
        return SingleNode(name, port, version, baseDir)
    }
}