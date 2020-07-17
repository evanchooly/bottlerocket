package com.antwerkz.bottlerocket.configuration.archived.mongo30.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.ConfigMode
import com.antwerkz.bottlerocket.configuration.Mode

class Journal(
      @Mode(ConfigMode.MONGOD) var enabled: Boolean? = null
) : ConfigBlock
