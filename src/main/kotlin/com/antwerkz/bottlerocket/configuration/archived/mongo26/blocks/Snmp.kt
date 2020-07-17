package com.antwerkz.bottlerocket.configuration.archived.mongo26.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.ConfigMode
import com.antwerkz.bottlerocket.configuration.Mode

class Snmp(
      @Mode(ConfigMode.MONGOD) var subagent: Boolean? = null,
      @Mode(ConfigMode.MONGOD) var master: Boolean? = null
) : ConfigBlock