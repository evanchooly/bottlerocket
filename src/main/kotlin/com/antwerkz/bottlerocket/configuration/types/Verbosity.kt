package com.antwerkz.bottlerocket.configuration.types

enum class Verbosity {
    INHERIT,
    ZERO,
    ONE,
    TWO,
    THREE,
    FOUR,
    FIVE;

    fun toConfigValue(): Int {
        return ordinal - 1;
    }

    override fun toString(): String {
        return "${toConfigValue()}"
    }
}