package com.antwerkz.bottlerocket.configuration.mongo42.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock

class ProcessManagement(
      var pidFilePath: String? = null,
      var fork: Boolean? = null,
      var windowsService: WindowsService = WindowsService()
) : ConfigBlock {
    fun windowsService(init: WindowsService.() -> Unit) {
        windowsService = initConfigBlock(WindowsService(), init)
    }
}