package com.antwerkz.bottlerocket.configuration.types

enum class LogDestination {
    SYSLOG,
    CONSOLE,
    FILE;

    override fun toString(): String {
        return name().toLowerCase()
    }

}