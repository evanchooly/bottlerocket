package com.antwerkz.bottlerocket.configuration.types

import java.util.Locale

enum class Compressor {
    DISABLED,
    NONE,
    SNAPPY,
    ZLIB;

    override fun toString(): String {
        return name.lowercase(Locale.getDefault())
    }
}
