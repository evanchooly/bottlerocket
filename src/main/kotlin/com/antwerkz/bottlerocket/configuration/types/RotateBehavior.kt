package com.antwerkz.bottlerocket.configuration.types

import java.util.Locale

enum class RotateBehavior {
    RENAME,
    ROTATE;

    override fun toString(): String {
        return name.lowercase(Locale.getDefault())
    }
}