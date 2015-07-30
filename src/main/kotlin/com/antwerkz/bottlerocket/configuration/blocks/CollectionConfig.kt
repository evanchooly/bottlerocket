package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.Compressor
import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.Since

class CollectionConfig(
      @Since("3.0.0") var blockCompressor: Compressor? = null
) : ConfigBlock