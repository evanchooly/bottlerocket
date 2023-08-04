package com.antwerkz.bottlerocket.configuration.types

import java.util.Locale

enum class SslMode {
    DISABLED {
        override fun toString(): String {
            return name.lowercase(Locale.getDefault())
        }
    },
    ALLOW,
    PREFER,
    REQUIRE;

    override fun toString(): String {
        return name.lowercase(Locale.getDefault()) + "SSL"
    }
}
