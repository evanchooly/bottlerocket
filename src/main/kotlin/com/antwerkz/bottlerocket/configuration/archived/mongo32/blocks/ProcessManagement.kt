package com.antwerkz.bottlerocket.configuration.archived.mongo32.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.mongo36.blocks.WindowsService

class ProcessManagement(
      var pidFilePath: String? = null,
      var fork: Boolean? = null,
      var windowsService: WindowsService = WindowsService()
) : ConfigBlock {
    fun windowsService(init: WindowsService.() -> Unit) {
        windowsService = initConfigBlock(WindowsService(), init)
    }
}