package com.antwerkz.bottlerocket.configuration.types

enum class ClusterAuthMode {
    KEY_FILE {
        override fun toString(): String {
            return "keyFile"
        }
    },
    SEND_KEY_FILE {
        override fun toString(): String {
            return "sendKeyFile"
        }
    },
    SEND_X509 {
        override fun toString(): String {
            return "sendX509"
        }
    },
    X509 {
        override fun toString(): String {
            return "x509"
        }
    }
}