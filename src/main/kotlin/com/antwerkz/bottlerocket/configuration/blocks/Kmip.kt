package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.Added
import com.antwerkz.bottlerocket.configuration.ConfigBlock

class Kmip(
    var keyIdentifier: String? = null,
    var rotateMasterKey: Boolean? = null,
    var serverName: String? = null,
    var port: String? = null,
    var clientCertificateFile: String? = null,
    var clientCertificatePassword: String? = null,
    @Added("4.0.0") var clientCertificateSelector: String? = null,
    var serverCAFile: String? = null,
    @Added("4.4.0") var connectRetries: Int? = null,
    @Added("4.4.0") var connectTimeoutMS: Int? = null
) : ConfigBlock
