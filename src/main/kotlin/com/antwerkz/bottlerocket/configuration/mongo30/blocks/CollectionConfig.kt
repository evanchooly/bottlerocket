package com.antwerkz.bottlerocket.configuration.mongo30.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.types.Compressor

class CollectionConfig() : ConfigBlock {
    var blockCompressor: Compressor? = null
}
