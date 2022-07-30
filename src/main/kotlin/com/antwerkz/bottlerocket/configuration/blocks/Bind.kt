package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.types.BindMethod

class Bind(
    var method: BindMethod? = null,
    var queryPassword: String? = null,
    var queryUser: String? = null,
    var saslMechanisms: String? = null,
    var useOSDefaults: Boolean? = null
) : ConfigBlock