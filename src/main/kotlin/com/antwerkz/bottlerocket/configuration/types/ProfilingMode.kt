package com.antwerkz.bottlerocket.configuration.types

import java.util.Locale

enum class ProfilingMode {
    OFF,
    ALL,
    SLOW_OP {
        override fun toString(): String {
            return "slowOp"
        }
    };

    override fun toString(): String {
        return name.lowercase(Locale.getDefault())
    }
}