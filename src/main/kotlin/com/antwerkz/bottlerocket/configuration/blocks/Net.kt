package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.*

class Net(
      var port: Int = 27017,
      var bindIp: String = "127.0.0.1",
      var maxIncomingConnections: Int = 65536,
      var wireObjectCheck: Boolean = true,
      var unixDomainSocket: UnixDomainSocket = UnixDomainSocket(),
      var ipv6: Boolean = false,
      var http: Http = Http(),
      var ssl: Ssl = Ssl()
) : ConfigBlock {
    fun unixDomainSocket(init: UnixDomainSocket.() -> Unit) {
        unixDomainSocket = initConfigBlock(UnixDomainSocket(), init)
    }

    fun http(init: Http.() -> Unit) {
        http = initConfigBlock(Http(), init)
    }

    fun ssl(init: Ssl.() -> Unit) {
        ssl = initConfigBlock(Ssl(), init)
    }
}