package com.antwerkz.bottlerocket

import com.github.zafarkhaja.semver.Version

enum class Versions {
    Version44 {
        override fun version(): Version {
            return Version.forIntegers(4, 4, 4)
        }
    },
    Version42 {
        override fun version(): Version {
            return Version.forIntegers(4, 2, 13)
        }
    },
    Version40 {
        override fun version(): Version {
            return Version.forIntegers(4, 0, 23)
        }
    },
    Version36 {
        override fun version(): Version {
            return Version.forIntegers(3, 6, 23)
        }
    };

    companion object {
        fun latest() = values().first().version()
        fun list() = values()
            .map { it.version() }
            .toList()
    }

    abstract fun version(): Version
}
