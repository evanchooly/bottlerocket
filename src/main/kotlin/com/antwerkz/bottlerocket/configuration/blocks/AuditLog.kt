package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.types.LogDestination
import com.antwerkz.bottlerocket.configuration.types.LogFormat

class AuditLog(
      var destination: LogDestination? = null,
      var format: LogFormat? = null,
      var path: String? = null,
      var filter: String? = null
) : ConfigBlock