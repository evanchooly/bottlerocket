package com.antwerkz.bottlerocket.executable

import com.antwerkz.bottlerocket.MongoManager
import com.antwerkz.bottlerocket.configuration.ConfigMode.MONGOD
import java.io.File

class Mongod internal constructor(manager: MongoManager, baseDir: File, name: String, port: Int)
    : MongoExecutable(manager, baseDir, name, port) {

    fun start() {
        if (!isAlive()) {
            logger.info("Starting mongod $name")

            val configFile = File(baseDir, "mongod.conf")
            config.writeConfig(configFile, MONGOD)

            exec(listOf(manager.mongod(), "--config", configFile.absolutePath))
        } else {
            logger.warn("start() was called on a running server: $port")
        }
    }
}