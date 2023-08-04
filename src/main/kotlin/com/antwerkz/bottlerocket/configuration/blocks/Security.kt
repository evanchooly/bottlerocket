package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.ConfigMode
import com.antwerkz.bottlerocket.configuration.Mode
import com.antwerkz.bottlerocket.configuration.types.ClusterAuthMode
import com.antwerkz.bottlerocket.configuration.types.State

class Security(
    var keyFile: String? = null,
    var clusterAuthMode: ClusterAuthMode? = null,
    @Mode(ConfigMode.MONGOD) var authorization: State? = null,
    var ldap: Ldap? = null,
    var sasl: Sasl? = null,
    var kmip: Kmip? = null,
    var javascriptEnabled: Boolean? = null,
    var enableEncryption: Boolean? = null,
    var encryptionCipherMode: String? = null,
    var encryptionKeyFile: String? = null,
    var clusterIpSourceWhitelist: List<String>? = null,
    var redactClientLogData: Boolean? = null,
    var transitionToAuth: Boolean? = null
) : ConfigBlock {
    fun sasl(init: Sasl.() -> Unit) {
        sasl = initConfigBlock(Sasl(), init)
    }

    fun ldap(init: Ldap.() -> Unit) {
        ldap = initConfigBlock(Ldap(), init)
    }

    fun kmip(init: Kmip.() -> Unit) {
        kmip = initConfigBlock(Kmip(), init)
    }
}
