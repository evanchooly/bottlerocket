package com.antwerkz.bottlerocket.configuration.types

enum class ProfilingMode {
    OFF,
    ALL,
    SLOW_OP {
        override fun toString(): String {
            return "slowOp"
        }
    };

    override fun toString(): String {
        return name().toLowerCase()
    }
}