package com.antwerkz.bottlerocket.configuration.mongo24

import com.antwerkz.bottlerocket.BaseVersionManager
import com.antwerkz.bottlerocket.ReplicaSet
import com.antwerkz.bottlerocket.configuration.ConfigMode
import com.antwerkz.bottlerocket.configuration.Configuration
import com.antwerkz.bottlerocket.executable.Mongod
import com.github.zafarkhaja.semver.Version
import java.io.File

class VersionManager24(version: Version) : BaseVersionManager(version) {
    override fun getReplicaSetConfig(primary: Mongod) = primary.getClient().getDatabase("local").getCollection(
          "system.replset").find().limit(1).first()

    override fun setReplicaSetName(node: Mongod, name: String) {
        val configuration = node.config as Configuration24
        configuration.replSet = name
    }

    override fun writeConfig(configFile: File, config: Configuration, mode: ConfigMode) {
        config.toProperties(mode).entrySet().forEach {
            configFile.appendText("${it.key} = ${it.value}\n")
        }
    }

    override fun initialConfig(baseDir: File, name: String, port: Int): Configuration {
        return configuration {
            this.port = port

            pidfilepath = File(baseDir, "${name}.pid").toString()
            dbpath = baseDir.getAbsolutePath()
            logpath = "${baseDir}/mongo.log"
        }
    }
}