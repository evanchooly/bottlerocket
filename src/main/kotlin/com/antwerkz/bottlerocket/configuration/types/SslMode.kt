package com.antwerkz.bottlerocket.configuration.types

enum class SslMode {
    DISABLED {
        override fun toString(): String {
            return name().toLowerCase()
        }
    },
    ALLOW,
    PREFER,
    REQUIRE;

    override fun toString(): String {
        return name().toLowerCase() + "SSL";
    }
}