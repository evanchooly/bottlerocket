package com.antwerkz.bottlerocket.configuration.mongo30

import com.antwerkz.bottlerocket.BaseVersionManager
import com.antwerkz.bottlerocket.ReplicaSet
import com.antwerkz.bottlerocket.configuration.Configuration
import com.antwerkz.bottlerocket.configuration.types.Destination.FILE
import com.antwerkz.bottlerocket.executable.Mongod
import com.github.zafarkhaja.semver.Version
import com.mongodb.ReadPreference
import org.bson.Document
import java.io.File
import java.util.ArrayList

class VersionManager30(version: Version) : BaseVersionManager(version) {
    override fun setReplicaSetName(node: Mongod, name: String) {
        val configuration = node.config as Configuration30
        configuration.replication.replSetName = name
    }

    override fun getReplicaSetConfig(primary: Mongod) = primary.runCommand(Document("replSetGetConfig", 1)).get("config") as Document

    override fun initialConfig(baseDir: File, name: String, port: Int): Configuration {
        return configuration {
            net {
                this.port = port
            }
            processManagement {
                pidFilePath = File(baseDir, "${name}.pid").toString()
            }
            storage {
                dbPath = baseDir.getAbsolutePath()
            }
            systemLog {
                destination = FILE
                path = "${baseDir}/mongo.log"
            }
        }
    }
}
