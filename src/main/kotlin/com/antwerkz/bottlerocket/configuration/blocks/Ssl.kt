package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.Added
import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.Removed
import com.antwerkz.bottlerocket.configuration.types.SslMode

class Ssl(
        @Removed("4.0.0")
        var sslOnNormalPorts: Boolean? = null,
        var mode: SslMode? = null,
        var PEMKeyFile: String? = null,
        var PEMKeyPassword: String? = null,
        @Added("4.0.0")
        var certificateSelector: String? = null,
        @Added("4.0.0")
        var clusterCertificateSelector: String? = null,
        var clusterFile: String? = null,
        var clusterCAFile: String? = null,
        var clusterPassword: String? = null,
        var CAFile: String? = null,
        var CRLFile: String? = null,
        var allowConnectionsWithoutCertificates: Boolean? = null,
        var allowInvalidCertificates: Boolean? = null,
        var allowInvalidHostnames: Boolean? = null,
        var disabledProtocols: String? = null,
        var FIPSMode: Boolean? = null
) : ConfigBlock