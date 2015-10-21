package com.antwerkz.bottlerocket.configuration.mongo26.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock

class ProcessManagement() : ConfigBlock {
    var pidFilePath: String? = null
    var fork: Boolean? = null
    var windowsService: WindowsService = WindowsService()

    fun windowsService(init: WindowsService.() -> Unit) {
        windowsService = initConfigBlock(WindowsService(), init)
    }
}