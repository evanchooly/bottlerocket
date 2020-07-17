package com.antwerkz.bottlerocket.configuration.mongo40.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock

class Kmip(
        var keyIdentifier: String? = null,
        var rotateMasterKey: Boolean? = null,
        var serverName: String? = null,
        var port: String? = null,
        var clientCertificateFile: String? = null,
        var clientCertificatePassword: String? = null,
        var serverCAFile: String? = null
) : ConfigBlock