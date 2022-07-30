package com.antwerkz.bottlerocket.executable

import com.antwerkz.bottlerocket.MongoManager
import com.antwerkz.bottlerocket.configuration.ConfigMode.MONGOS
import java.io.File

class Mongos internal constructor(manager: MongoManager, baseDir: File, name: String, port: Int)
    : MongoExecutable(manager, baseDir, name, port) {

    fun start() {
        if (!isAlive()) {
            logger.info("Starting mongos $name (${manager.version})")
            config.writeConfig(File(baseDir, "mongos.conf"), MONGOS)
            exec(listOf(manager.mongos(), "--config", File(baseDir, "mongos.conf").absolutePath))
        } else {
            logger.warn("start() was called on a running mongos: $name")
        }
    }
}