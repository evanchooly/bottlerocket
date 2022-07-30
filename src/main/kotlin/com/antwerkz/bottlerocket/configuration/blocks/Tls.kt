package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.Added
import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.types.TlsMode

class Tls(
    @Added("4.2.0")
    var CAFile: String? = null,
    @Added("4.2.0")
    var CRLFile: String? = null,
    @Added("4.2.0")
    var FIPSMode: Boolean? = null,
    @Added("4.2.0")
    var allowConnectionsWithoutCertificates: Boolean? = null,
    @Added("4.2.0")
    var allowInvalidCertificates: Boolean? = null,
    @Added("4.2.0")
    var allowInvalidHostnames: Boolean? = null,
    @Added("4.2.0")
    var certificateKeyFile: String? = null,
    @Added("4.2.0")
    var certificateKeyFilePassword: String? = null,
    @Added("4.2.0")
    var certificateSelector: String? = null,
    @Added("4.2.0")
    var clusterCAFile: String? = null,
    @Added("4.2.0")
    var clusterCertificateSelector: String? = null,
    @Added("4.2.0")
    var clusterFile: String? = null,
    @Added("4.2.0")
    var clusterPassword: String? = null,
    @Added("4.2.0")
    var disabledProtocols: String? = null,
    @Added("4.2.0")
    var mode: TlsMode? = null
) : ConfigBlock