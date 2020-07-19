package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock

class ProcessManagement(
    var pidFilePath: String? = null,
    var fork: Boolean? = null,
    var windowsService: WindowsService = WindowsService(),
    var timeZoneInfo: String? = null
) : ConfigBlock {
    fun windowsService(init: WindowsService.() -> Unit) {
        windowsService = initConfigBlock(WindowsService(), init)
    }
}