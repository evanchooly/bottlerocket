package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.ConfigMode
import com.antwerkz.bottlerocket.configuration.Verbosity
import com.github.zafarkhaja.semver.Version

class Journal(
      var enabled: Boolean? = null
) : ConfigBlock