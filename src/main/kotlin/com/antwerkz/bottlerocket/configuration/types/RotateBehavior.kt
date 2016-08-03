package com.antwerkz.bottlerocket.configuration.types

enum class RotateBehavior {
    RENAME,
    ROTATE;

    override fun toString(): String {
        return name.toLowerCase()
    }
}