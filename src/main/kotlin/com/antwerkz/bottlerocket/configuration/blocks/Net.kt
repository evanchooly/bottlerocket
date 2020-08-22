package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.Added
import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.Removed
import com.antwerkz.bottlerocket.configuration.types.ServiceExecutor

class Net(
    var port: Int? = 27017,
    var bindIp: String? = "127.0.0.1",
    @Added("3.6.0")
    var bindIpAll: Boolean? = null,
    var compression: Compression? = null,
    var ipv6: Boolean? = null,
    var maxIncomingConnections: Int? = null,
    @Added("3.6.0")
    var serviceExecutor: ServiceExecutor? = null,
    @Removed("4.0.0")
    var transportLayer: String? = null,
    var ssl: Ssl? = null,
    var tls: Tls? = null,
    var unixDomainSocket: UnixDomainSocket? = null,
    var wireObjectCheck: Boolean? = null
) : ConfigBlock {
    fun unixDomainSocket(init: UnixDomainSocket.() -> Unit) {
        unixDomainSocket = initConfigBlock(UnixDomainSocket(), init)
    }

    fun ssl(init: Ssl.() -> Unit) {
        ssl = initConfigBlock(Ssl(), init)
    }

    fun tls(init: Tls.() -> Unit) {
        tls = initConfigBlock(Tls(), init)
    }

    fun compression(init: Compression.() -> Unit) {
        compression = initConfigBlock(Compression(), init)
    }
}