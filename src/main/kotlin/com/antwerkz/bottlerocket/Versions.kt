package com.antwerkz.bottlerocket

import com.github.zafarkhaja.semver.Version

enum class Versions {
    Version44 {
        override fun version(): Version {
            return Version.forIntegers(4, 4, 2)
        }
    },
    Version42 {
        override fun version(): Version {
            return Version.forIntegers(4, 2, 11)
        }
    },
    Version40 {
        override fun version(): Version {
            return Version.forIntegers(4, 0, 21)
        }
    },
    Version36 {
        override fun version(): Version {
            return Version.forIntegers(3, 6, 21)
        }
    };

    companion object {
        fun latest() = Version44.version()
        fun list() = values()
            .map { it.version() }
            .toList()
    }

    abstract fun version(): Version

}