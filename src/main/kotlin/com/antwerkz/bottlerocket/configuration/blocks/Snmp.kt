package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.ConfigMode
import com.antwerkz.bottlerocket.configuration.Mode

class Snmp(
      Mode(ConfigMode.MONGOD) var subagent: Boolean = true,
      Mode(ConfigMode.MONGOD) var master: Boolean = false
) : ConfigBlock