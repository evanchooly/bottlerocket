package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.Added
import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.ConfigMode
import com.antwerkz.bottlerocket.configuration.Mode
import com.antwerkz.bottlerocket.configuration.types.ProfilingMode

class OperationProfiling(
    @Mode(ConfigMode.MONGOD) var slowOpThresholdMs: Int? = null,
    @Mode(ConfigMode.MONGOD) var slowOpSampleRate: Double? = null,
    @Mode(ConfigMode.MONGOD) var mode: ProfilingMode? = null,
    @Mode(ConfigMode.MONGOD) @Added("4.4.2") var filter: String? = null,
) : ConfigBlock
