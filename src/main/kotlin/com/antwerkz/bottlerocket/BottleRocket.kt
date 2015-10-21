package com.antwerkz.bottlerocket

import java.io.File

public val TEMP_DIR = if (File("/tmp").exists()) "/tmp" else System.getProperty("java.io.tmpdir")
public val DEFAULT_NAME = "rocket"
public val DEFAULT_PORT = 30000
public val DEFAULT_VERSION = "3.0.5"
public val DEFAULT_BASE_DIR = File("${TEMP_DIR}/${DEFAULT_NAME}")

