package com.antwerkz.bottlerocket.configuration

enum class IndexPrefetch {
    NONE,
    ALL,
    _ID_ONLY;

    override fun toString(): String {
        return name().toLowerCase()
    }
}