package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.Since

class Http(
      public Since("2.6") var enabled: Boolean = false,
      var JSONPEnabled: Boolean = false,
      var RESTInterfaceEnabled: Boolean = false
) : ConfigBlock