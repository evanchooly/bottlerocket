package com.antwerkz.bottlerocket.configuration.types

enum class State {
    ENABLED,
    DISABLED;

    override fun toString(): String {
        return name().toLowerCase()
    }
}