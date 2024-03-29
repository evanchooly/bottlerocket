package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock

class UnixDomainSocket(
    var enabled: Boolean? = null,
    var pathPrefix: String? = null,
    var filePermissions: Int? = null
) : ConfigBlock
