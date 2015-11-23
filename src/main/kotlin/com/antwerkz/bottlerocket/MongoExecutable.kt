package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.configuration.Configuration
import com.jayway.awaitility.Awaitility
import com.mongodb.MongoClient
import com.mongodb.MongoClientOptions
import com.mongodb.MongoCommandException
import com.mongodb.MongoCredential
import com.mongodb.MongoSocketReadException
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
    val config: Configuration
    abstract val logger: Logger
    private var client: MongoClient? = null

    companion object {
        val SUPER_USER = "superuser"
        val SUPER_USER_PASSWORD = "rocketman"
        private val LOG = LoggerFactory.getLogger(MongoExecutable::class.java)
    }

    init {
        config = manager.initialConfig(baseDir, name, port)
    }

    fun isAuthEnabled(): Boolean {
        return config.isAuthEnabled()
    }

    fun isAlive(): Boolean {
        return process?.isAlive() ?: false;
    }

    fun getServerAddress(): ServerAddress {
        return ServerAddress("localhost", port)
    }

    fun clean() {
        baseDir.deleteTree()
    }

    fun shutdown() {
        shutdownWithDriver()
        Thread.sleep(3000)
        client?.close()
        client = null
    }

    fun shutdownWithShell() {
        if (isAlive()) {
            LOG.info("Shutting down service on port ${port}")
            try {
                Awaitility
                      .await()
                      .atMost(10, TimeUnit.SECONDS)
                      .until({ runCommand("db.shutdownServer()") })
            } catch(e: Exception) {
                e.printStackTrace()
                LOG.warn("Timed out waiting for server to stop.  Forcibly killing instead.", e)
            }

            process?.destroy(true)
            File(baseDir, "mongod.lock").delete()
        }
    }

    fun shutdownWithDriver() {
        if (isAlive()) {
            LOG.info("Shutting down service on port ${port}")
            try {
                Awaitility
                      .await()
                      .atMost(10, TimeUnit.SECONDS)
                      .until({ getClient(isAuthEnabled()).runCommand(Document("shutdown", 1)) })
            } catch(e: Exception) {
                if ( e.cause !is MongoSocketReadException) {
                    LOG.warn("Failed to shutdown server.  Forcibly killing instead.", e)
                    process?.destroy(true)
                } else if ( e.cause is MongoCommandException) {
                    throw e
                }
            }

            process?.destroy(false)
            File(baseDir, "mongod.lock").delete()
        }
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


