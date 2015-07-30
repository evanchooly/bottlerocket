package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.ClusterRole
import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.ConfigMode
import com.antwerkz.bottlerocket.configuration.Mode
import com.antwerkz.bottlerocket.configuration.Since

class Sharding(
      @Mode(ConfigMode.MONGOD) var clusterRole: ClusterRole? = null,
      @Since("2.4.0") var archiveMovedChunks: Boolean? = null,
      var autoSplit: Boolean? = null,
      var configDB: String? = null,
      var chunkSize: Int? = null
) : ConfigBlock