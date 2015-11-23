package com.antwerkz.bottlerocket.clusters

import com.antwerkz.bottlerocket.*
import java.io.File

abstract class MongoClusterBuilder<out T>() {
    var name: String = BottleRocket.DEFAULT_NAME
        private set
    var port: Int = BottleRocket.DEFAULT_PORT
        private set
    var version: String = BottleRocket.DEFAULT_VERSION
        private set
    var baseDir: File = BottleRocket.DEFAULT_BASE_DIR
        private set

    open fun name(value: String): T {
        name = value
        baseDir = if (baseDir == BottleRocket.DEFAULT_BASE_DIR) File(
              "${BottleRocket.TEMP_DIR}/${name}") else baseDir
        return this as T
    }

    fun port(value: Int): T {
        port = value
        return this as T
    }

    fun version(value: String): T {
        version = value
        return this as T
    }

    fun baseDir(value: File): T {
        baseDir = value
        return this as T
    }
}