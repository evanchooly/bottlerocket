package com.antwerkz.bottlerocket.configuration.types

import java.util.Locale

enum class State {
    ENABLED,
    DISABLED;

    override fun toString(): String {
        return name.lowercase(Locale.getDefault())
    }
}