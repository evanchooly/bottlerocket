package com.antwerkz.bottlerocket

import java.io.File

object BottleRocket {
    @JvmField val TEMP_DIR = if (File("/tmp").exists()) "/tmp" else System.getProperty("java.io.tmpdir")
    @JvmField val DEFAULT_NAME = "rocket"
    @JvmField val DEFAULT_PORT = 30000
    @JvmField val DEFAULT_VERSION = "3.0.5"
    @JvmField val DEFAULT_BASE_DIR = File("${TEMP_DIR}/${DEFAULT_NAME}")
}

