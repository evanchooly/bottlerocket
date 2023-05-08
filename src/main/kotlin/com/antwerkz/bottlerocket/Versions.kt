package com.antwerkz.bottlerocket

import com.github.zafarkhaja.semver.Version

enum class Versions {
    Version60 {
        override fun version(): Version = Version.forIntegers(6, 0, 5)
    },
    Version50 {
        override fun version(): Version = Version.forIntegers(5, 0, 15)
    },
    Version44 {
        override fun version(): Version = Version.forIntegers(4, 4, 19)
    },
    Version42 {
        override fun version(): Version = Version.forIntegers(4, 2, 24)
    },
    Version40 {
        override fun version(): Version = Version.forIntegers(4, 0, 28)
    };

    companion object {
        @JvmStatic
        fun latest() = values().first().version()
        @JvmStatic
        fun list() = values()
            .map { it.version() }
            .toList()
    }

    abstract fun version(): Version
}
