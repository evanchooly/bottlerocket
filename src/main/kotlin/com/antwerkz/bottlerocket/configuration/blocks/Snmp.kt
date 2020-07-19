package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.Added
import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.ConfigMode
import com.antwerkz.bottlerocket.configuration.Mode

class Snmp(
    @Added("4.0.0")
    @Mode(ConfigMode.MONGOD) var disabled: Boolean? = null,
    @Mode(ConfigMode.MONGOD) var subagent: Boolean? = null,
    @Mode(ConfigMode.MONGOD) var master: Boolean? = null
) : ConfigBlock