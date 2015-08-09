package com.antwerkz.bottlerocket.configuration.types

enum class DiagnosticLog(val level: Int) {
    NONE(0),
    WRITE(1),
    READ(2),
    READ_AND_WRITE(3),
    WRITE_AND_SOME_READ(7);

    override fun toString(): String {
        return level.toString()
    }
}