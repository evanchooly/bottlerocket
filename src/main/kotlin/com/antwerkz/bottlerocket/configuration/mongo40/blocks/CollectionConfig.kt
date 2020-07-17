package com.antwerkz.bottlerocket.configuration.mongo40.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.types.Compressor

class CollectionConfig(
      var blockCompressor: Compressor? = null
) : ConfigBlock