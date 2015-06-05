package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.*

class Security(
      var keyFile: String? = null,
      Since("2.6.0") var clusterAuthMode: ClusterAuthMode = ClusterAuthMode.KEY_FILE,
      Mode(ConfigMode.MONGOD) var authorization: State = State.DISABLED,
      var sasl: Sasl = Sasl(),
      var javascriptEnabled: Boolean = true
) : ConfigBlock {
    fun sasl(init: Sasl.() -> Unit) {
        sasl = initConfigBlock(Sasl(), init)
    }
}