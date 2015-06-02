package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock

class UnixDomainSocket(
      var enabled: Boolean = true,
      var pathPrefix: String = "/tmp"
) : ConfigBlock