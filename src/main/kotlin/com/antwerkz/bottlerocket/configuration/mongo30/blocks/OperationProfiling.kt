package com.antwerkz.bottlerocket.configuration.mongo30.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.ConfigMode
import com.antwerkz.bottlerocket.configuration.Mode
import com.antwerkz.bottlerocket.configuration.types.ProfilingMode

class OperationProfiling() : ConfigBlock {
    @Mode(ConfigMode.MONGOD) var slowOpThresholdMs: Int? = null
    @Mode(ConfigMode.MONGOD) var mode: ProfilingMode? = null
}
