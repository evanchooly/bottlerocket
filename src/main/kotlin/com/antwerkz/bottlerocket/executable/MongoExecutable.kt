package com.antwerkz.bottlerocket.executable

import com.antwerkz.bottlerocket.MongoManager
import com.antwerkz.bottlerocket.clusters.Configurable
import com.antwerkz.bottlerocket.configuration.Configuration
import com.antwerkz.bottlerocket.runCommand
import com.jayway.awaitility.Awaitility
import com.jayway.awaitility.Duration.ONE_SECOND
import com.mongodb.MongoClientSettings
import com.mongodb.MongoCommandException
import com.mongodb.MongoSocketReadException
import com.mongodb.ServerAddress
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.bson.Document
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.process.PidProcess
import org.zeroturnaround.process.Processes
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

abstract class MongoExecutable
    internal constructor(internal val manager: MongoManager, val baseDir: File, val name: String, val port: Int) : Configurable {
    private var process: PidProcess? = null
    private lateinit var client: MongoClient
    internal var config = manager.initialConfig(baseDir, name, port)
        private set
    val logger: Logger = LoggerFactory.getLogger("${this::class.simpleName}.$port")

    companion object {
        val SUPER_USER = "superuser"
        val SUPER_USER_PASSWORD = "rocketman"
        private val LOG = LoggerFactory.getLogger(MongoExecutable::class.java)
    }

    override fun configure(update: Configuration) {
        config = config.update(update)
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
        baseDir.deleteRecursively()
    }

    fun shutdown() {
//        shutdownWithDriver()
        client.close()
        shutdownWithKill()
        Awaitility
            .await()
            .atMost(30, TimeUnit.SECONDS)
            .pollInterval(ONE_SECOND)
            .until<Boolean> { !isAlive() }
        File(baseDir, "mongod.lock").delete()
    }

    fun shutdownWithDriver() {
        if (isAlive()) {
            LOG.debug("Shutting down service on port $port")
            try {
                getClient().runCommand("{ shutdown: 1 }")
            } catch (e: Exception) {
                if (e.cause !is MongoSocketReadException) {
                    LOG.warn("Failed to shutdown server.  Forcibly killing instead.", e)
                    process?.destroy(true)
                } else if (e.cause is MongoCommandException) {
                    throw e
                }
            }
        }
    }

    fun shutdownWithKill() {
        if (isAlive()) {
            logger.info("Stopping ${this::class.java.simpleName.toLowerCase()} $name")
            process?.destroy(true)
        }
    }

    fun runCommand(command: String): Document {
        return getClient().runCommand(command)
    }

    fun getClient(): MongoClient {
        if (!::client.isInitialized) {
            this.client = MongoClients.create(MongoClientSettings.builder()
                .applyToClusterSettings { builder ->
                    builder.hosts(listOf(getServerAddress()))
                }
                .build())
        }

        return client
    }

    fun waitForStartUp() {
        Awaitility
            .await()
            .atMost(30, TimeUnit.SECONDS)
            .pollInterval(3, TimeUnit.SECONDS)
            .until<Boolean> {
                try {
                    getClient().getDatabase("admin")
                        .listCollectionNames()
                    true
                } catch (e: Throwable) {
                    false
                }
            }
    }

    protected fun exec(args: List<String>) {
        logger.debug("Starting process with this command: $args")
        val processResult = ProcessExecutor()
            .command(args)
//            .redirectOutput(of(logger).asDebug())
//            .redirectError(of(logger).asError())
            .redirectOutput(FileOutputStream(File(baseDir, "mongo.log")))
            .redirectError(FileOutputStream(File(baseDir, "mongo.err")))
            .destroyOnExit()
            .start()

        process = Processes.newPidProcess(processResult?.process)

        waitForStartUp()
        if (process == null || !(process?.isAlive ?: false)) {
            throw IllegalStateException("process for ${manager.version} on $port should be alive: $process")
        }
    }

    override fun toString(): String {
        var s = "${javaClass.simpleName}:$port"
        if (isAuthEnabled()) {
            s += ", auth:true"
        }
        if (isAlive()) {
            s += ", alive:true"
        }
        return s
    }
}
