package com.antwerkz.bottlerocket.configuration.mongo22

import com.antwerkz.bottlerocket.BaseVersionManager
import com.antwerkz.bottlerocket.DatabaseRole
import com.antwerkz.bottlerocket.MongoExecutable
import com.antwerkz.bottlerocket.configuration.ConfigMode
import com.antwerkz.bottlerocket.configuration.mongo24.VersionManager24
import com.antwerkz.bottlerocket.executable.Mongod
import com.github.zafarkhaja.semver.Version
import com.mongodb.MongoClient
import org.bson.Document
import java.io.File
import com.antwerkz.bottlerocket.configuration.Configuration as BaseConfiguration

class VersionManager22(version: Version) : BaseVersionManager(version) {
    override fun enableAuth(node: MongoExecutable, pemFile: String?) {
        throw UnsupportedOperationException("Version ${version} authentication is not supported at this time");
        /*
                node.config.merge(configuration {
                    auth = true
                    keyFile = pemFile
                })
        */
    }

    override fun addAdminUser(client: MongoClient) {
        addUser(client, "admin", MongoExecutable.SUPER_USER, MongoExecutable.SUPER_USER_PASSWORD,
              listOf(DatabaseRole("root", "admin"), DatabaseRole("userAdminAnyDatabase", "admin"),
                    DatabaseRole("readWriteAnyDatabase", "admin")))
    }

    override fun addUser(client: MongoClient, database: String, userName: String, password: String, roles: List<DatabaseRole>) {
        val document = Document("user", userName)
              .append("pwd", VersionManager24.hashUserNamePassword(userName, password))
        client.getDatabase(database)
              .getCollection("system.users")
              .insertOne(document)
    }

    override fun getReplicaSetConfig(client: MongoClient): Document {
        return client.getDatabase("local")
              .getCollection("system.replset")
              .find()
              .limit(1)
              .first()
    }

    override fun setReplicaSetName(node: Mongod, name: String) {
        val configuration = node.config as Configuration
        configuration.replSet = name
    }

    override fun writeConfig(configFile: File, config: BaseConfiguration, mode: ConfigMode) {
        val fileWriter = configFile.writer()
        config.toProperties(mode).entrySet().forEach {
            fileWriter.write("${it.key} = ${it.value}\n")
        }
        fileWriter.close()
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