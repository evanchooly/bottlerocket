package com.antwerkz.bottlerocket.configuration.mongo32.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock

class InMemory(
        var engineConfig: EngineConfig = EngineConfig()
) : ConfigBlock {
    fun engineConfig(init: EngineConfig.() -> Unit) {
        engineConfig = initConfigBlock(EngineConfig(), init)
    }

    class EngineConfig(var inMemorySizeGB: Int? = null) : ConfigBlock
}