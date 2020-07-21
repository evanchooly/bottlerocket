package com.antwerkz.bottlerocket

import com.github.zafarkhaja.semver.Version
import org.slf4j.LoggerFactory
import java.io.File

object BottleRocket {
    private val LOG = LoggerFactory.getLogger(BottleRocket::class.java)

    @JvmField
    val TEMP_DIR = System.getProperty("java.io.tmpdir")

    @JvmField
    val DEFAULT_NAME = "rocket"

    @JvmField
    val DEFAULT_PORT = 30000

    @JvmField
    val DEFAULT_VERSION = Version.forIntegers(4, 2, 8)

    @JvmField
    val DEFAULT_BASE_DIR = File("$TEMP_DIR/$DEFAULT_NAME")

    @JvmField
    var linuxVersion = "rhel80"

    init {
        val etc = File("/etc")
        if (File(etc, "redhat-release").exists()) {
            LOG.info("RedHat Linux detected")
            linuxVersion = "rhel80"
        } else if (File(etc, "lsb-release").exists()) {
            LOG.info("Ubuntu Linux detected")
            linuxVersion = "ubuntu1804"
        }
    }
}
