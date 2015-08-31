package com.antwerkz.bottlerocket.configuration.mongo24

import com.antwerkz.bottlerocket.configuration.Configuration as BaseConfiguration
import com.antwerkz.bottlerocket.BaseVersionManager
import com.antwerkz.bottlerocket.DatabaseRole
import com.antwerkz.bottlerocket.MongoExecutable
import com.antwerkz.bottlerocket.configuration.ConfigMode
import com.antwerkz.bottlerocket.configuration.mongo24.Configuration
import com.antwerkz.bottlerocket.executable.Mongod
import com.github.zafarkhaja.semver.Version
import com.mongodb.MongoClient
import org.bson.Document
import java.io.File
import java.math.BigInteger
import java.security.MessageDigest

class VersionManager24(version: Version) : BaseVersionManager(version) {
    companion object {
        fun hashUserNamePassword(userName: String, password: String): String {
            // username + ":mongo:" + password
            val m = MessageDigest.getInstance("MD5");
            val data = (userName + ":mongo:" + password).toByteArray();
            m.update(data, 0, data.size());
            val i = BigInteger(1, m.digest());
            return i.toString(16)
        }
    }

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
              listOf(DatabaseRole("root"), DatabaseRole("adminAnyDatabase"), DatabaseRole("userAdminAnyDatabase")))
    }

    override fun addUser(client: MongoClient, database: String, userName: String, password: String,
                         roles: List<DatabaseRole>) {
        val map = roles.groupBy { it.database }
        map.forEach { entry ->
            var targetDb = database
            val document = Document("user", userName)
                  .append("roles", entry.value.map { it.role })
            if(entry.key == null) {
                document.append("pwd", hashUserNamePassword(userName, password))
            } else {
                targetDb = entry.key!!
                document.append("userSource", database)
            }
            client.getDatabase(targetDb)
                  .getCollection("system.users")
                  .insertOne(document)

        }
    }

    override fun getReplicaSetConfig(client: MongoClient) = client.getDatabase("local")
          .getCollection("system.replset")
          .find()
          .limit(1)
          .first()

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