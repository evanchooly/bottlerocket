package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.ConfigMode
import com.antwerkz.bottlerocket.configuration.Mode
import com.antwerkz.bottlerocket.configuration.types.ClusterRole

class Sharding(
    @Mode(ConfigMode.MONGOD) var clusterRole: ClusterRole? = null,
    var archiveMovedChunks: Boolean? = null,
    @Mode(ConfigMode.MONGOS) var configDB: String? = null
) : ConfigBlock
