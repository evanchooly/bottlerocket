package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.clusters.deleteTree
import com.antwerkz.bottlerocket.configuration.Configuration
import com.jayway.awaitility.Awaitility
import com.jayway.awaitility.Duration
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
import org.zeroturnaround.process.PidProcess
import java.io.File
import java.util.concurrent.TimeUnit
import com.antwerkz.bottlerocket.configuration.mongo24.Configuration as Config24
import com.antwerkz.bottlerocket.configuration.mongo24.configuration as config24
import com.antwerkz.bottlerocket.configuration.mongo26.Configuration as Config26
import com.antwerkz.bottlerocket.configuration.mongo26.configuration as config26
import com.antwerkz.bottlerocket.configuration.mongo30.Configuration as Config30
import com.antwerkz.bottlerocket.configuration.mongo30.configuration as config30

abstract class MongoExecutable(val manager: MongoManager, val name: String, val port: Int, val baseDir: File) {
    protected var process: PidProcess? = null
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
        return process?.isAlive ?: false
    }

    fun getServerAddress(): ServerAddress {
        return ServerAddress("localhost", port)
    }

    fun clean() {
        baseDir.deleteTree()
    }

    open fun shutdown() {
//        shutdownWithDriver()
        shutdownWithKill()
        val process = process!!
        Awaitility
                .await()
                .atMost(30, TimeUnit.SECONDS)
                .pollInterval(Duration.ONE_SECOND)
                .until<Boolean>({ !process.isAlive })
        File(baseDir, "mongod.lock").delete()
        client?.close()
        client = null
    }

    fun shutdownWithDriver() {
        if (isAlive()) {
            LOG.info("Shutting down service on port ${port}")
            try {
                getClient(isAuthEnabled())
                        .runCommand(Document("shutdown", 1))
            } catch(e: Exception) {
                if ( e.cause !is MongoSocketReadException) {
                    LOG.warn("Failed to shutdown server.  Forcibly killing instead.", e)
                    process?.destroy(true)
                } else if ( e.cause is MongoCommandException) {
                    throw e
                }
            }
        }
    }

    fun shutdownWithKill() {
        if (isAlive()) {
            LOG.info("Shutting down service on port ${port}")
            process?.destroy(true)
        }
    }

    fun runCommand(command: Document): Document {
        return getClient().runCommand(command)
    }

    fun getClient(authEnabled: Boolean = this.isAuthEnabled()): MongoClient {
        if (client == null) {
            val credentials = if (authEnabled) {
                arrayListOf(MongoCredential.createCredential(MongoExecutable.SUPER_USER, "admin",
                        MongoExecutable.SUPER_USER_PASSWORD.toCharArray()))
            } else {
                listOf<MongoCredential>()
            }

            client = MongoClient(getServerAddress(), credentials, MongoClientOptions.builder()
//                    .maxWaitTime(1000)
                    .readPreference(ReadPreference.primaryPreferred())
                    .build())
        }

        return client!!
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
        var s = "${javaClass.simpleName}:${port}"
        if (isAuthEnabled()) {
            s += ", auth:true"
        }
        if (isAlive()) {
            s += ", alive:true"
        }
        return s
    }
}


