package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock

class Ldap(
    var servers: String? = null,
    var timeoutMS: Int? = null,
    var transportSecurity: String? = null,
    var userToDNMapping: String? = null,
    var validateLDAPServerConfig: Boolean? = null,
    var authz: Authz? = null,
    var bind: Bind? = null
) : ConfigBlock {
    fun authz(init: Authz.() -> Unit) {
        authz = initConfigBlock(Authz(), init)
    }

    fun bind(init: Bind.() -> Unit) {
        bind = initConfigBlock(Bind(), init)
    }
}