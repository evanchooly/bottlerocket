package com.antwerkz.bottlerocket.configuration

enum class RotateBehavior {
    RENAME,
    ROTATE;

    override fun toString(): String {
        return name().toLowerCase()
    }
}