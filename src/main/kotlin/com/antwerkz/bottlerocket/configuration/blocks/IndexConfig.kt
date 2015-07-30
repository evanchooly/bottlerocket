package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.Since

class IndexConfig(
      @Since("3.0.0") var prefixCompression: Boolean? = null
) : ConfigBlock