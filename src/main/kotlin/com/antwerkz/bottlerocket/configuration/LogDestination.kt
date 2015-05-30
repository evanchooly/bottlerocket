package com.antwerkz.bottlerocket.configuration

enum class LogDestination {
    SYSLOG,
    CONSOLE,
    FILE;

    override fun toString(): String {
        return name().toLowerCase()
    }

}