package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.blocks.WindowsService

class ProcessManagement(
      var pidFilePath: String? = null,
      var fork: Boolean? = null,
      var windowsService: WindowsService = WindowsService()
) : ConfigBlock {
    fun windowsService(init: WindowsService.() -> Unit) {
        windowsService = initConfigBlock(WindowsService(), init)
    }
}