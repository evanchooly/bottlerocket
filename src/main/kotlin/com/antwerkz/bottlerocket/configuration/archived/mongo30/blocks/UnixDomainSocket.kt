package com.antwerkz.bottlerocket.configuration.archived.mongo30.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock

class UnixDomainSocket(
      var enabled: Boolean? = null,
      var pathPrefix: String? = null,
      var filePermissions: Int? = null
) : ConfigBlock