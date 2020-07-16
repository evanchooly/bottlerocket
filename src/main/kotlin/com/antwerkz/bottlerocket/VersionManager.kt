package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.configuration.ConfigMode
import com.antwerkz.bottlerocket.configuration.Configuration
import com.antwerkz.bottlerocket.configuration.mongo26.VersionManager26
import com.antwerkz.bottlerocket.configuration.mongo30.VersionManager30
import com.antwerkz.bottlerocket.configuration.mongo32.VersionManager32
import com.github.zafarkhaja.semver.Version
import com.mongodb.ReadPreference
import com.mongodb.client.MongoClient
import org.bson.Document
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

interface VersionManager {
    val version: Version
    fun writeConfig(configFile: File, config: Configuration, mode: ConfigMode = ConfigMode.MONGOD)
    fun initialConfig(baseDir: File, name: String, port: Int): Configuration

    fun getReplicaSetConfig(client: MongoClient): Document?

    fun enableAuth(node: MongoExecutable, pemFile: String? = null)

    fun addUser(client: MongoClient, database: String, userName: String, password: String, roles: List<DatabaseRole>)
    fun addAdminUser(client: MongoClient)
}

abstract class BaseVersionManager(override val version: Version) : VersionManager {
    companion object {
        val LOG: Logger = LoggerFactory.getLogger(BaseVersionManager::class.java)

        fun of(version: Version): VersionManager {
            return when ("${version.majorVersion}.${version.minorVersion}") {
                "3.2" -> VersionManager32(version)
                "3.0" -> VersionManager30(version)
                "2.6" -> VersionManager26(version)
                else -> throw IllegalArgumentException("Unsupported version $version")
            }
        }
    }

    override fun writeConfig(configFile: File, config: Configuration, mode: ConfigMode) {
        configFile.writeText(config.toYaml(mode))
    }


}

fun MongoClient.runCommand(command: Document, readPreference: ReadPreference = ReadPreference.primary()): Document {
    try {
        return getDatabase("admin")
              .runCommand(command, readPreference)
    } catch(e: Exception) {
        throw RuntimeException("command failed: ${command} with preference ${readPreference}", e)
    }
}
