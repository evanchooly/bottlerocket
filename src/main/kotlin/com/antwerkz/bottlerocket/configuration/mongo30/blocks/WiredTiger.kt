package com.antwerkz.bottlerocket.configuration.mongo30.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock

class WiredTiger() : ConfigBlock {

    var collectionConfig: CollectionConfig = CollectionConfig()
    var engineConfig: EngineConfig = EngineConfig()
    var indexConfig: IndexConfig = IndexConfig()

    fun collectionConfig(init: CollectionConfig.() -> Unit) {
        collectionConfig = initConfigBlock(CollectionConfig(), init)
    }

    fun engineConfig(init: EngineConfig.() -> Unit) {
        engineConfig = initConfigBlock(EngineConfig(), init)
    }

    fun indexConfig(init: IndexConfig.() -> Unit) {
        indexConfig = initConfigBlock(IndexConfig(), init)
    }
}