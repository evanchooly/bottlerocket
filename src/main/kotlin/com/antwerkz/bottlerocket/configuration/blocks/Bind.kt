package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock

class Bind(
        var method: Method? = null,
        var queryPassword: String? = null,
        var queryUser: String? = null,
        var saslMechanisms: String? = null,
        var useOSDefaults: Boolean? = null
) : ConfigBlock

enum class Method {
    SIMPLE,
    SASL
}
