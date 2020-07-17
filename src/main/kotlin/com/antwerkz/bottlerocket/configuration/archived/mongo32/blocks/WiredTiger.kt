package com.antwerkz.bottlerocket.configuration.archived.mongo32.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.mongo36.blocks.CollectionConfig
import com.antwerkz.bottlerocket.configuration.mongo36.blocks.EngineConfig
import com.antwerkz.bottlerocket.configuration.mongo36.blocks.IndexConfig

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