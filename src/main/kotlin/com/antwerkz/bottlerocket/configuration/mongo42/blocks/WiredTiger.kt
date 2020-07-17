package com.antwerkz.bottlerocket.configuration.mongo42.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock

class WiredTiger(
        var collectionConfig: CollectionConfig = CollectionConfig(),
        var engineConfig: EngineConfig = EngineConfig(),
        var indexConfig: IndexConfig = IndexConfig()
) : ConfigBlock {

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