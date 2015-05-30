package com.antwerkz.bottlerocket.configuration

enum class State {
    ENABLED
    DISABLED

    override fun toString(): String {
        return name().toLowerCase()
    }
}