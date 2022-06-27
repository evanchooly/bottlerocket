package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock

class WiredTiger(
    var collectionConfig: CollectionConfig? = null,
    var engineConfig: EngineConfig? = null,
    var indexConfig: IndexConfig? = null
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