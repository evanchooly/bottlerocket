package com.antwerkz.bottlerocket.configuration.mongo26.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock

class Sasl(
      var hostName: String? = null,
      var serviceName: String? = null,
      var saslauthdSocketPath: String? = null
) : ConfigBlock