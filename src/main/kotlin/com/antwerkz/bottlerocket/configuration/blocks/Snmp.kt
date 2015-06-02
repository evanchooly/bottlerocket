package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock

class Snmp(
      var subAgent: Boolean = true,
      var master: Boolean = false
) : ConfigBlock