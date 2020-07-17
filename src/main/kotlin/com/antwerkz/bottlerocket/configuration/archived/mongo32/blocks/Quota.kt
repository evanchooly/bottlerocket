package com.antwerkz.bottlerocket.configuration.archived.mongo32.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.ConfigMode
import com.antwerkz.bottlerocket.configuration.Mode

class Quota(
      @Mode(ConfigMode.MONGOD) var enforced: Boolean? = null,
      @Mode(ConfigMode.MONGOD) var maxFilesPerDB: Int? = null
) : ConfigBlock