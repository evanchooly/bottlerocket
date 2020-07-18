package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.types.Compressor

class EngineConfig(
      var cacheSizeGB: Double? = null,
      var maxCacheOverflowFileSizeGB: Double? = null,
//      var statisticsLogDelaySecs: Int? = null,
      var journalCompressor: Compressor? = null,
      var directoryForIndexes: Boolean? = null

) : ConfigBlock