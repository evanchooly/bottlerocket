package com.antwerkz.bottlerocket.configuration.types

import java.util.Locale

enum class IndexPrefetch {
    NONE,
    ALL,
    _ID_ONLY;

    override fun toString(): String {
        return name.lowercase(Locale.getDefault())
    }
}