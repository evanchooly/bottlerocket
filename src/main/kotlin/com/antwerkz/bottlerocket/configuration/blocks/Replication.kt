package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.ConfigMode
import com.antwerkz.bottlerocket.configuration.Mode
import com.antwerkz.bottlerocket.configuration.Removed
import com.antwerkz.bottlerocket.configuration.types.IndexPrefetch

class Replication(
    @Mode(ConfigMode.MONGOD) var oplogSizeMB: Int? = null,
    @Mode(ConfigMode.MONGOD) var replSetName: String? = null,
    @Removed("4.2.0")
    @Mode(ConfigMode.MONGOD) var secondaryIndexPrefetch: IndexPrefetch? = null,
    var localPingThresholdMs: Int? = null,
    var enableMajorityReadConcern: Boolean? = null
) : ConfigBlock