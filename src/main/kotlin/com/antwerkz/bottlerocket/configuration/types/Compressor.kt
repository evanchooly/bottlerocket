package com.antwerkz.bottlerocket.configuration.types

enum class Compressor {
    DISABLED,
    NONE,
    SNAPPY,
    ZLIB;

    override fun toString(): String {
        return name.toLowerCase()
    }
}