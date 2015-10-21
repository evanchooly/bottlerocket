package com.antwerkz.bottlerocket.configuration.mongo30.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock

class Net() : ConfigBlock {
      var port: Int? = 27017
      var bindIp: String? = "127.0.0.1"
      var maxIncomingConnections: Int? = null
      var wireObjectCheck: Boolean? = null
      var unixDomainSocket: UnixDomainSocket = UnixDomainSocket()
      var ipv6: Boolean? = null
      var http: Http = Http()
      var ssl: Ssl = Ssl()

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