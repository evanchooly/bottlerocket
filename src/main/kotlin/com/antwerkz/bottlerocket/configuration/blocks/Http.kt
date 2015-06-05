package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.ConfigMode
import com.antwerkz.bottlerocket.configuration.Mode
import com.antwerkz.bottlerocket.configuration.Since

class Http(
      Since("2.6.0") var enabled: Boolean = false,
      Mode(ConfigMode.MONGOD) var JSONPEnabled: Boolean = false,
      Mode(ConfigMode.MONGOD) var RESTInterfaceEnabled: Boolean = false
) : ConfigBlock