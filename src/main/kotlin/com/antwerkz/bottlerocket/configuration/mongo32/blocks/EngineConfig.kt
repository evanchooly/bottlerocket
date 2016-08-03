package com.antwerkz.bottlerocket.configuration.mongo32.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.types.Compressor

class EngineConfig(
      var cacheSizeGB: Int? = null,
//      var statisticsLogDelaySecs: Int? = null,
      var journalCompressor: Compressor? = null,
      var directoryForIndexes: Boolean? = null

) : ConfigBlock