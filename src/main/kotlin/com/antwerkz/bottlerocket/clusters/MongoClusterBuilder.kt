package com.antwerkz.bottlerocket.clusters

import com.antwerkz.bottlerocket.*
import java.io.File

abstract class MongoClusterBuilder<out T>() {
    var name: String = DEFAULT_NAME
        private set
    var port: Int = DEFAULT_PORT
        private set
    var version: String = DEFAULT_VERSION
        private set
    var baseDir: File = DEFAULT_BASE_DIR
        private set

    open fun name(value: String): T {
        name = value
        baseDir = if (baseDir == DEFAULT_BASE_DIR) File(
              "${TEMP_DIR}/${name}") else baseDir
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