package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.*

class Security(
      var keyFile: String? = null,
      @Since("2.6.0") var clusterAuthMode: ClusterAuthMode? = null,
      @Mode(ConfigMode.MONGOD) var authorization: State? = null,
      var sasl: Sasl = Sasl(),
      var javascriptEnabled: Boolean? = null
) : ConfigBlock {
    fun sasl(init: Sasl.() -> Unit) {
        sasl = initConfigBlock(Sasl(), init)
    }
}