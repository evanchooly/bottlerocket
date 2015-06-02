package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.IndexPrefetch
import com.antwerkz.bottlerocket.configuration.Since

class Replication(
      var oplogSizeMB: Int? = null,
      var replSetName: String? = null,
      Since("2.2") var secondaryIndexPrefetch: IndexPrefetch = IndexPrefetch.ALL,
      var localPingThresholdMs: Int = 15

) : ConfigBlock