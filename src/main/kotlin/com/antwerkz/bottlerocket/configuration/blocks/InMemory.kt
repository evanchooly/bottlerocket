package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock

class InMemory(
    var engineConfig: EngineConfig? = null
) : ConfigBlock {
    fun engineConfig(init: EngineConfig.() -> Unit) {
        engineConfig = initConfigBlock(EngineConfig(), init)
    }

    class EngineConfig(var inMemorySizeGB: Int? = null) : ConfigBlock
}