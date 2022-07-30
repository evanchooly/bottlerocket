package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock

class SetParameter(
    var ldapUserCacheInvalidationInterval: Int? = null
) : ConfigBlock