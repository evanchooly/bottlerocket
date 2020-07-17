package com.antwerkz.bottlerocket.configuration.archived.mongo32.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock

class UnixDomainSocket(
      var enabled: Boolean? = null,
      var pathPrefix: String? = null,
      var filePermissions: Int? = null
) : ConfigBlock