package com.antwerkz.bottlerocket.configuration.mongo26.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock

class UnixDomainSocket() : ConfigBlock {
      var enabled: Boolean? = null
      var pathPrefix: String? = null
}
