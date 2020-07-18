package com.antwerkz.bottlerocket

import com.github.zafarkhaja.semver.Version
import java.io.File

object BottleRocket {
    @JvmField
    val TEMP_DIR= System.getProperty("java.io.tmpdir")
    @JvmField
    val DEFAULT_NAME = "rocket"
    @JvmField
    val DEFAULT_PORT = 30000
    @JvmField
    val DEFAULT_VERSION = Version.forIntegers(4, 2, 8)
    @JvmField
    val DEFAULT_BASE_DIR = File("${TEMP_DIR}/${DEFAULT_NAME}")
}

