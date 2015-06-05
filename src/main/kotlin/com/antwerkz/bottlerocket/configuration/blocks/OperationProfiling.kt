package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.ConfigMode
import com.antwerkz.bottlerocket.configuration.Mode
import com.antwerkz.bottlerocket.configuration.ProfilingMode

class OperationProfiling(
      Mode(ConfigMode.MONGOD) var slowOpThresholdMs: Int = 100,
      Mode(ConfigMode.MONGOD) var mode: ProfilingMode = ProfilingMode.OFF
) : ConfigBlock