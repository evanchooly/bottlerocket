package com.antwerkz.bottlerocket.configuration.archived.mongo26.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.types.SslMode

class Ssl(
      @Deprecated("Deprecated since version 2.6.")
      var sslOnNormalPorts: Boolean? = null,
      var disabledProtocols: String? = null,
      var mode: SslMode? = null,
      var PEMKeyFile: String? = null,
      var PEMKeyPassword: String? = null,
      var clusterFile: String? = null,
      var clusterPassword: String? = null,
      var CAFile: String? = null,
      var CRLFile: String? = null,
      var allowInvalidCertificates: Boolean? = null,
      var FIPSMode: Boolean? = null,
      var weakCertificateValidation: Boolean? = null
) : ConfigBlock