package com.antwerkz.bottlerocket

import com.github.zafarkhaja.semver.Version

enum class Versions {
    //    Version44 {
//        override fun version(): Version {
//            return Version.forIntegers(4, 4, 0)
//        }
//    },
    Version42 {
        override fun version(): Version {
            return Version.forIntegers(4, 2, 9)
        }
    },
    Version40 {
        override fun version(): Version {
            return Version.forIntegers(4, 0, 20)
        }
    },
    Version36 {
        override fun version(): Version {
            return Version.forIntegers(3, 6, 19)
        }
    };

    companion object {
        fun list() = values()
            .map { it.version() }
            .toList()
    }

    abstract fun version(): Version
}