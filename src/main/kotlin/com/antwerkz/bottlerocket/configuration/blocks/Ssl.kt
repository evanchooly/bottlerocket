package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.Since
import com.antwerkz.bottlerocket.configuration.SslMode

class Ssl(
      deprecated("Deprecated since version 2.6.")
      var sslOnNormalPorts: Boolean = false,
      Since("2.6.0") var mode: SslMode = SslMode.DISABLED,
      Since("2.2.0") var PEMKeyFile: String? = null,
      Since("2.2.0") var PEMKeyPassword: String? = null,
      Since("2.6.0") var clusterFile: String? = null,
      Since("2.6.0") var clusterPassword: String? = null,
      Since("2.4.0") var CAFile: String? = null,
      Since("2.4.0") var CRLFile: String? = null,
      Since("2.4.0") var allowConnectionsWithoutCertificates: Boolean = false,
      Since("2.6.0") var allowInvalidCertificates: Boolean = false,
      Since("3.0.0") var allowInvalidHostnames: Boolean = false,
      Since("2.4.0") var FIPSMode: Boolean = false,
      var setParameter: Map<String, String> = mapOf()  // TODO
) : ConfigBlock