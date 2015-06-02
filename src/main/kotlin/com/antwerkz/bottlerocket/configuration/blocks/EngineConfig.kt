package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.*

class EngineConfig(
      Since("3.0.0") var cacheSizeGB: Int? = null,
      Since("3.0.0") var statisticsLogDelaySecs: Int = 0,
      Since("3.0.0") var journalCompressor: Compressor = Compressor.SNAPPY,
      Since("3.0.0") var directoryForIndexes: Boolean = false,
      Since("3.0.0") var collectionConfig: CollectionConfig = CollectionConfig(),
      Since("3.0.0") var indexConfig: IndexConfig = IndexConfig()

) : ConfigBlock {

    fun collectionConfig(init: CollectionConfig.() -> Unit) {
        collectionConfig = initConfigBlock(CollectionConfig(), init)
    }

    fun indexConfig(init: IndexConfig.() -> Unit) {
        indexConfig = initConfigBlock(IndexConfig(), init)
    }
}