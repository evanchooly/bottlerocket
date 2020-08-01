package com.antwerkz.bottlerocket

import com.github.zafarkhaja.semver.Version
import org.slf4j.LoggerFactory
import java.io.File

object BottleRocket {
    private val LOG = LoggerFactory.getLogger(BottleRocket::class.java)

    @JvmField
    val TEMP_DIR = if(File("buld").exists()) "build" else "target"

    @JvmField
    val DEFAULT_NAME = "rocket"

    @JvmField
    val DEFAULT_PORT = 30000

    @JvmField
    val DEFAULT_VERSION = Version.forIntegers(4, 2, 8)

    @JvmField
    val DEFAULT_BASE_DIR = File("$TEMP_DIR/$DEFAULT_NAME")
}
