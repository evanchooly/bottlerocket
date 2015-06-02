package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock

class WindowsService(
      var serviceName: String = "MongoDB",
      var displayName: String = "MongoDB",
      var description: String = "MongoDB Server",
      var serviceUser: String? = null,
      var servicePassword: String? = null
) : ConfigBlock