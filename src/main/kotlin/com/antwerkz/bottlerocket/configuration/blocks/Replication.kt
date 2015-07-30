package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.ConfigMode
import com.antwerkz.bottlerocket.configuration.IndexPrefetch
import com.antwerkz.bottlerocket.configuration.Mode
import com.antwerkz.bottlerocket.configuration.Since

class Replication(
      @Mode(ConfigMode.MONGOD) var oplogSizeMB: Int? = 10,
      @Mode(ConfigMode.MONGOD) var replSetName: String? = null,
      @Since("2.2.0") @Mode(ConfigMode.MONGOD) var secondaryIndexPrefetch: IndexPrefetch? = null,
      var localPingThresholdMs: Int? = null

) : ConfigBlock