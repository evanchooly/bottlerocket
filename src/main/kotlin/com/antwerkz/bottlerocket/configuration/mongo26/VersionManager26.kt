package com.antwerkz.bottlerocket.configuration.mongo26

import com.antwerkz.bottlerocket.BaseVersionManager
import com.antwerkz.bottlerocket.DatabaseRole
import com.antwerkz.bottlerocket.MongoExecutable
import com.antwerkz.bottlerocket.configuration.Configuration
import com.antwerkz.bottlerocket.configuration.types.Destination.FILE
import com.antwerkz.bottlerocket.configuration.types.State.ENABLED
import com.github.zafarkhaja.semver.Version
import com.mongodb.MongoClient
import org.bson.Document
import java.io.File

open class VersionManager26(version: Version) : BaseVersionManager(version) {

    override fun enableAuth(node: MongoExecutable, pemFile: String?) {
        node.config.merge(configuration {
            security {
                authorization = ENABLED
                keyFile = pemFile
            }
        })
    }

    override fun addAdminUser(client: MongoClient) {
         addUser(client, "admin", MongoExecutable.SUPER_USER, MongoExecutable.SUPER_USER_PASSWORD,
               listOf(DatabaseRole("root", "admin"),
                     DatabaseRole("userAdminAnyDatabase", "admin"),
                     DatabaseRole("readWriteAnyDatabase", "admin")))
     }

     override fun addUser(client: MongoClient, database: String, userName: String, password: String, roles: List<DatabaseRole>) {
         client.getDatabase(database).
               runCommand(Document("createUser", userName)
                     .append("pwd", password)
                     .append("roles", roles.map { it.toDB() }))
     }

    override fun getReplicaSetConfig(client: MongoClient): Document? {
        return client.getDatabase("local")
                .getCollection("system.replset").find().first()
    }

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