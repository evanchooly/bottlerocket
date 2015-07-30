package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.*

class EngineConfig(
      @Since("3.0.0") var cacheSizeGB: Int? = null,
      @Since("3.0.0") var statisticsLogDelaySecs: Int? = null,
      @Since("3.0.0") var journalCompressor: Compressor? = null,
      @Since("3.0.0") var directoryForIndexes: Boolean? = null

) : ConfigBlock