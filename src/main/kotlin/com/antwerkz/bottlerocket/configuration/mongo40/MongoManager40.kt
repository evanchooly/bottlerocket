package com.antwerkz.bottlerocket.configuration.mongo40

import com.antwerkz.bottlerocket.MongoManager
import com.antwerkz.bottlerocket.configuration.types.Destination.FILE
import com.github.zafarkhaja.semver.Version
import java.io.File

class MongoManager40(version: Version) : MongoManager(version) {
    override var linuxBaseUrl = "https://fastdl.mongodb.org/linux/mongodb-linux-x86_64-ubuntu1804-"
    override var macBaseUrl = "https://fastdl.mongodb.org/osx/mongodb-osx-ssl-x86_64-"
    override var windowsBaseUrl = "https://fastdl.mongodb.org/win32/mongodb-win32-x86_64-2008plus-ssl-"

    override fun initialConfig(baseDir: File, name: String, port: Int): Configuration {
        return configuration {
            net {
                this.port = port
            }
            processManagement {
                pidFilePath = File(baseDir, "${name}.pid").toString()
            }
            storage {
                dbPath = baseDir.absolutePath
            }
            systemLog {
                destination = FILE
                path = "${baseDir}/mongo.log"
            }
        }
    }

    fun configuration(init: Configuration.() -> Unit): Configuration {
        val configuration = Configuration()
        configuration.init()
        return configuration
    }

}
