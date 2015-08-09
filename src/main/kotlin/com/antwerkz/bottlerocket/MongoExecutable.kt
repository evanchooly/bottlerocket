package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.types.Destination
import com.antwerkz.bottlerocket.configuration.types.State
import com.jayway.awaitility.Awaitility
import com.mongodb.MongoClient
import com.mongodb.MongoClientOptions
import com.mongodb.MongoCredential
import com.mongodb.MongoSocketReadException
import com.mongodb.MongoSocketReadTimeoutException
import com.mongodb.ReadPreference
import com.mongodb.ServerAddress
import org.bson.Document
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.process.JavaProcess
import java.io.ByteArrayInputStream
import java.io.File
import java.util.concurrent.TimeUnit
import com.antwerkz.bottlerocket.configuration.mongo24.Configuration as Config24
import com.antwerkz.bottlerocket.configuration.mongo24.configuration as config24
import com.antwerkz.bottlerocket.configuration.mongo26.Configuration as Config26
import com.antwerkz.bottlerocket.configuration.mongo26.configuration as config26
import com.antwerkz.bottlerocket.configuration.mongo30.Configuration as Config30
import com.antwerkz.bottlerocket.configuration.mongo30.configuration as config30

public abstract class MongoExecutable(val manager: MongoManager, val name: String, val port: Int, val baseDir: File) {
    public var process: JavaProcess? = null
        protected set
    val config: ConfigBlock
    abstract val logger: Logger
    private var client: MongoClient? = null

    companion object {
        val SUPER_USER = "superuser"
        val SUPER_USER_PASSWORD = "rocketman"
        private val LOG = LoggerFactory.getLogger(javaClass<MongoExecutable>())
    }

    init {
        config = config30 {
            net {
                this.port = this@MongoExecutable.port
            }
            processManagement {
                pidFilePath = File(baseDir, "${name}.pid").toString()
            }
            storage {
                dbPath = baseDir.getAbsolutePath()
            }
            systemLog {
                destination = Destination.FILE
                path = "${baseDir}/mongod.log"
            }
        }
    }

    fun enableAuth(pemFile: String? = null) {
        config.merge(config30 {
            security {
                authorization = if (pemFile == null) State.ENABLED else State.DISABLED
                keyFile = pemFile
            }
        })
    }

    fun isAuthEnabled(): Boolean {
        return if (config is Config30) {
            config.security.authorization == State.ENABLED || config.security.keyFile != null
        } else if (config is Config26) {
            config.security.authorization == State.ENABLED || config.security.keyFile != null
        } else if (config is Config24) {
            config.auth?: false  || config.keyFile != null
        } else {
            false
        }
    }

    fun isAlive(): Boolean {
        return process?.isAlive() ?: false;
    }

    fun getServerAddress(): ServerAddress {
        return ServerAddress("localhost", port)
    }

    fun clean() {
        shutdown()
        baseDir.deleteTree()
    }

    fun shutdown() {
        shutdownWithShell()
    }

    fun shutdownWithKill() {
        client?.close()
        client = null
        if (isAlive()) {
            LOG.info("Shutting down service on port ${port}")
            process?.destroy(true)
            File(baseDir, "mongod.lock").delete()
        }
    }

    fun shutdownWithShell() {
        client?.close()
        client = null
        if (isAlive()) {
            LOG.info("Shutting down service on port ${port}")
            runCommand("db.shutdownServer()")
            process?.destroy(true)
            File(baseDir, "mongod.lock").delete()
        }
    }

    fun shutdownWithDriver() {
        if (isAlive()) {
            LOG.info("Shutting down service on port ${port}")
            try {
                runCommand(Document("shutdown", 0))
            } catch(ignored: MongoSocketReadTimeoutException) {
            } catch(ignored: MongoSocketReadException) {
                // this happens because we're shutting down the server and can't read the result of the command
            }
            process?.destroy(true)
            waitForShutdown()
            File(baseDir, "mongod.lock").delete()
        }
        client?.close()
        client = null
    }

    private fun runCommand(command: String, authEnabled: Boolean = this.isAuthEnabled()) {
        val list = command(authEnabled)
        var commandString = ""
        if (authEnabled) {
            commandString = "db.auth(\"${MongoExecutable.SUPER_USER}\", \"${MongoExecutable.SUPER_USER_PASSWORD}\");\n"
        }
        commandString += command
        ProcessExecutor()
              .command(list)
              .redirectInput(ByteArrayInputStream(commandString.toByteArray()))
              .execute()
    }

    private fun command(authEnabled: Boolean): List<String> {
        val list = arrayListOf(manager.mongo,
              "admin", "--port", "${this.port}", "--quiet")
        if (authEnabled) {
            list.addAll(arrayOf("--username", MongoExecutable.SUPER_USER, "--password", MongoExecutable.SUPER_USER_PASSWORD,
                  "--authenticationDatabase", "admin"))
        }
        return list
    }

    fun getClient(authEnabled: Boolean = this.isAuthEnabled()): MongoClient {
        if (client == null) {
            var credentials = if (authEnabled) {
                arrayListOf(MongoCredential.createCredential(MongoExecutable.SUPER_USER, "admin",
                      MongoExecutable.SUPER_USER_PASSWORD.toCharArray()))
            } else {
                listOf<MongoCredential>()
            }

            client = MongoClient(getServerAddress(), credentials, MongoClientOptions.builder()
                  .maxWaitTime(1000)
                  .readPreference(ReadPreference.primaryPreferred())
                  .build())
        }

        return client!!;
    }

    fun addRootUser() {
        runCommand(Document("createUser", MongoExecutable.SUPER_USER)
              .append("pwd", MongoExecutable.SUPER_USER_PASSWORD)
              .append("roles", listOf("root")))
    }

    fun waitForStartUp() {
        Awaitility
              .await()
              .atMost(30, TimeUnit.SECONDS)
              .pollInterval(3, TimeUnit.SECONDS)
              .until<Boolean>({
                  tryConnect()
              })
    }

    fun waitForShutdown() {
        Awaitility
              .await()
              .atMost(30, TimeUnit.SECONDS)
              .pollInterval(3, TimeUnit.SECONDS)
              .until<Boolean>({
                  !tryConnect()
              })
    }

    fun tryConnect(): Boolean {
        try {
            getClient().getDatabase("admin")
                  .listCollectionNames()
            return true
        } catch(e: Throwable) {
            return false
        }
    }

    fun runCommand(command: Document, readPreference: ReadPreference = ReadPreference.primary()): Document {
        return getClient().getDatabase("admin")
              .runCommand(command, readPreference)
    }

    override fun toString(): String {
        var s = "${javaClass.getSimpleName()}:${port}"
        if (isAuthEnabled()) {
            s += ", auth:true"
        }
        if (isAlive()) {
            s += ", alive:true"
        }
        return s
    }
}


