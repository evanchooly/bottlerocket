package com.antwerkz.bottlerocket.configuration.mongo40.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.ConfigMode
import com.antwerkz.bottlerocket.configuration.Mode
import com.antwerkz.bottlerocket.configuration.types.ClusterAuthMode
import com.antwerkz.bottlerocket.configuration.types.State

class Security(
        var keyFile: String? = null,
        var clusterAuthMode: ClusterAuthMode? = null,
        @Mode(ConfigMode.MONGOD)
      var authorization: State? = null,
        var sasl: Sasl = Sasl(),
        var kmip: Kmip = Kmip(),
        var javascriptEnabled: Boolean? = null,
        var enableEncryption: Boolean? = null,
        var encryptionCipherMode: String? = null,
        var encryptionKeyFile: String? = null
) : ConfigBlock {
    fun sasl(init: Sasl.() -> Unit) {
        sasl = initConfigBlock(Sasl(), init)
    }
    fun kmip(init: Kmip.() -> Unit) {
        kmip = initConfigBlock(Kmip(), init)
    }
}