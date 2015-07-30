package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.Since

class WiredTiger(
      @Since("3.0.0") var collectionConfig: CollectionConfig = CollectionConfig(),
      @Since("3.0.0") var engineConfig: EngineConfig = EngineConfig(),
      @Since("3.0.0") var indexConfig: IndexConfig = IndexConfig()
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