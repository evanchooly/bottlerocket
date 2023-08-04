package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.configuration.Configuration
import com.antwerkz.bottlerocket.configuration.configuration
import com.antwerkz.bottlerocket.executable.MongoExecutable
import com.antwerkz.bottlerocket.executable.Mongod
import com.antwerkz.bottlerocket.executable.Mongos
import com.github.zafarkhaja.semver.Version
import com.mongodb.client.MongoClient
import java.io.File
import org.apache.commons.lang3.SystemUtils
import org.bson.Document
import org.slf4j.LoggerFactory

internal class MongoManager(val version: Version) {
    companion object {
        val log = LoggerFactory.getLogger(MongoManager::class.java)
    }
    internal val extension = if (SystemUtils.IS_OS_WINDOWS) ".exe" else ""
    private val mongoDistribution = MongoDistribution.of(version)
    private val binDir = mongoDistribution.binDir
    internal fun mongo() = "$binDir/mongo${extension}"
    internal fun mongod() = "$binDir/mongod${extension}"
    internal fun mongos() = "$binDir/mongos${extension}"
    fun initialConfig(baseDir: File, name: String, port: Int): Configuration {
        return configuration {
            net {
                bindIp = "127.0.0.1"
                this.port = port
            }
            processManagement { pidFilePath = File(baseDir, "$name.pid").path }
            storage { dbPath = baseDir.path }
        }
    }

    fun deleteBinaries() {
        File(binDir).parentFile.deleteRecursively()
    }

    fun getReplicaSetConfig(client: MongoClient): Document? {
        return client.getDatabase("local").getCollection("system.replset").find().first()
    }

    /*
        fun enableAuth(node: MongoExecutable, pemFile: String? = null) {
            node.config.merge(configuration {
                security {
                    authorization = ENABLED
                    keyFile = pemFile
                }
            })
        }
    */
    fun addUser(
        client: MongoClient,
        database: String,
        userName: String,
        password: String,
        roles: List<DatabaseRole>
    ) {
        if (!hasUser(client, database, userName)) {
            try {
                client
                    .getDatabase(database)
                    .runCommand(
                        Document("createUser", userName)
                            .append("pwd", password)
                            .append("roles", roles.map { it.toDB() })
                    )
            } catch (e: Exception) {
                throw e
            }
        }
    }

    private fun hasUser(client: MongoClient, database: String, userName: String): Boolean {
        val document = client.getDatabase(database).runCommand(Document("usersInfo", userName))
        return (document["users"] as List<*>?)?.isNotEmpty() ?: false
    }

    fun addAdminUser(client: MongoClient) {
        addUser(
            client,
            "admin",
            MongoExecutable.SUPER_USER,
            MongoExecutable.SUPER_USER_PASSWORD,
            listOf(
                DatabaseRole("root", "admin"),
                DatabaseRole("userAdminAnyDatabase", "admin"),
                DatabaseRole("readWriteAnyDatabase", "admin")
            )
        )
    }

    internal fun mongod(baseDir: File, name: String, port: Int): Mongod {
        return Mongod(this, baseDir, name, port)
    }

    internal fun mongos(baseDir: File, name: String, port: Int): Mongos {
        return Mongos(this, baseDir, name, port)
    }
}
