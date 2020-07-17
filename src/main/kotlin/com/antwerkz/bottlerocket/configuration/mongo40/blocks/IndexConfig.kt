package com.antwerkz.bottlerocket.configuration.mongo40.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock

class IndexConfig(
      var prefixCompression: Boolean? = null
) : ConfigBlock