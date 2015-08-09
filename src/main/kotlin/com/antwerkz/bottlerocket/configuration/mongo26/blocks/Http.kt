package com.antwerkz.bottlerocket.configuration.mongo26.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.ConfigMode
import com.antwerkz.bottlerocket.configuration.Mode

class Http(
      var enabled: Boolean? = null,
      @Mode(ConfigMode.MONGOD) var JSONPEnabled: Boolean? = null,
      @Mode(ConfigMode.MONGOD) var RESTInterfaceEnabled: Boolean? = null
) : ConfigBlock