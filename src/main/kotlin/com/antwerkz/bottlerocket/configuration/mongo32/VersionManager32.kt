package com.antwerkz.bottlerocket.configuration.mongo32

import com.antwerkz.bottlerocket.BaseVersionManager
import com.antwerkz.bottlerocket.DatabaseRole
import com.antwerkz.bottlerocket.MongoExecutable
import com.antwerkz.bottlerocket.configuration.Configuration as BaseConfiguration
import com.antwerkz.bottlerocket.configuration.types.Destination.FILE
import com.antwerkz.bottlerocket.configuration.types.State.ENABLED
import com.antwerkz.bottlerocket.executable.Mongod
import com.antwerkz.bottlerocket.runCommand
import com.github.zafarkhaja.semver.Version
import com.mongodb.MongoClient
import org.bson.Document
import java.io.File

class VersionManager32(version: Version) : BaseVersionManager(version) {
    override fun setReplicaSetName(node: Mongod, name: String) {
        val configuration = node.config as Configuration
        configuration.replication.replSetName = name
    }

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

    override fun getReplicaSetConfig(client: MongoClient) = client.runCommand(Document("replSetGetConfig", 1)).get("config") as Document

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
