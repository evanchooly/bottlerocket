package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.LogDestination
import com.antwerkz.bottlerocket.configuration.LogFormat
import com.antwerkz.bottlerocket.configuration.Since

class AuditLog(
      @Since("2.6.0") var destination: LogDestination? = null,
      @Since("2.6.0") var format: LogFormat? = null,
      @Since("2.6.0") var path: String? = null,
      @Since("2.6.0") var filter: String? = null
) : ConfigBlock