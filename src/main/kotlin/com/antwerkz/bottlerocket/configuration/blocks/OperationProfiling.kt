package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.ProfilingMode

class OperationProfiling(
      var slowOpThresholdMs: Int = 100,
      var mode: ProfilingMode = ProfilingMode.OFF
) : ConfigBlock