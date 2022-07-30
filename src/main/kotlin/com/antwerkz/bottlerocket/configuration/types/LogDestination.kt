package com.antwerkz.bottlerocket.configuration.types

import java.util.Locale

enum class LogDestination {
    SYSLOG,
    CONSOLE,
    FILE;

    override fun toString(): String {
        return name.lowercase(Locale.getDefault())
    }
}