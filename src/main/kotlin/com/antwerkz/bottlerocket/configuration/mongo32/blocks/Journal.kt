package com.antwerkz.bottlerocket.configuration.mongo32.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.ConfigMode
import com.antwerkz.bottlerocket.configuration.Mode

class Journal(
      @Mode(ConfigMode.MONGOD)
      var enabled: Boolean? = null,
      var commitIntervalMs: Int? = null
) : ConfigBlock
