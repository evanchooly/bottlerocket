package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock

class WindowsService(
    var serviceName: String? = null,
    var displayName: String? = null,
    var description: String? = null,
    var serviceUser: String? = null,
    var servicePassword: String? = null
) : ConfigBlock
