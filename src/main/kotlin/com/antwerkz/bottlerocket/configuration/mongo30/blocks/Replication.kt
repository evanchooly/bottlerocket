package com.antwerkz.bottlerocket.configuration.mongo30.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.ConfigMode
import com.antwerkz.bottlerocket.configuration.Mode
import com.antwerkz.bottlerocket.configuration.types.IndexPrefetch

class Replication() : ConfigBlock {
    @Mode(ConfigMode.MONGOD) var oplogSizeMB: Int? = 10
    @Mode(ConfigMode.MONGOD) var replSetName: String? = null
    @Mode(ConfigMode.MONGOD) var secondaryIndexPrefetch: IndexPrefetch? = null
    var localPingThresholdMs: Int? = null
}

