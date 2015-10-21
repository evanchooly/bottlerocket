package com.antwerkz.bottlerocket.configuration.mongo30.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.ConfigMode
import com.antwerkz.bottlerocket.configuration.Mode
import com.antwerkz.bottlerocket.configuration.types.ClusterAuthMode
import com.antwerkz.bottlerocket.configuration.types.State

class Security() : ConfigBlock {
    var keyFile: String? = null
    var clusterAuthMode: ClusterAuthMode? = null
    @Mode(ConfigMode.MONGOD) var authorization: State? = null
    var sasl: Sasl = Sasl()
    var javascriptEnabled: Boolean? = null

    fun sasl(init: Sasl.() -> Unit) {
        sasl = initConfigBlock(Sasl(), init)
    }
}