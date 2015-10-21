package com.antwerkz.bottlerocket.configuration.mongo26.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.ConfigMode
import com.antwerkz.bottlerocket.configuration.Mode

class Journal() : ConfigBlock {
    @Mode(ConfigMode.MONGOD) var enabled: Boolean? = null
    @Mode(ConfigMode.MONGOD) var debugFlags: Int? = null
    @Mode(ConfigMode.MONGOD) var commitIntervalMs: Int? = null
}
