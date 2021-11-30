package com.antwerkz.bottlerocket

import com.github.zafarkhaja.semver.Version

enum class Versions {
    Version50 {
        override fun version(): Version {
            return Version.forIntegers(5, 0, 4)
        }
    },
    Version44 {
        override fun version(): Version {
            return Version.forIntegers(4, 4, 10)
        }
    },
    Version42 {
        override fun version(): Version {
            return Version.forIntegers(4, 2, 17)
        }
    },
    Version40 {
        override fun version(): Version {
            return Version.forIntegers(4, 0, 27)
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
