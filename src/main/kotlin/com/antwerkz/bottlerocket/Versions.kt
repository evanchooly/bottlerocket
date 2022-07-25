package com.antwerkz.bottlerocket

import com.github.zafarkhaja.semver.Version

enum class Versions {
    Version60 {
        override fun version(): Version = Version.forIntegers(6, 0, 0)
    },
    Version50 {
        override fun version(): Version = Version.forIntegers(5, 0, 9)
    },
    Version44 {
        override fun version(): Version = Version.forIntegers(4, 4, 15)
    },
    Version42 {
        override fun version(): Version = Version.forIntegers(4, 2, 21)
    },
    Version40 {
        override fun version(): Version = Version.forIntegers(4, 0, 28)
    };

    companion object {
        fun latest() = values().first().version()
        fun list() = values()
            .map { it.version() }
            .toList()
    }

    abstract fun version(): Version
}
