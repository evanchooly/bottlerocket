package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.configuration.ConfigMode.MONGOS
import com.antwerkz.bottlerocket.configuration.Configuration
import com.antwerkz.bottlerocket.configuration.Destination
import com.antwerkz.bottlerocket.configuration.configuration
import com.antwerkz.bottlerocket.executable.ConfigServer
import com.antwerkz.bottlerocket.executable.Mongod
import com.mongodb.ServerAddress
import org.bson.BsonDocument
import org.bson.codecs.BsonDocumentCodec
import org.bson.codecs.DecoderContext
import org.bson.json.JsonReader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream
import org.zeroturnaround.process.JavaProcess
import org.zeroturnaround.process.Processes
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

public open class MongoExecutable(val manager: MongoManager, val name: String, val port: Int, val baseDir: File) {
    public var process: JavaProcess? = null
        protected set
    val config: Configuration
    public var authEnabled: Boolean = false

    companion object {
        val SUPER_USER = "superuser"
        val SUPER_USER_PASSWORD = "rocketman"
        private val LOG = LoggerFactory.getLogger(javaClass<MongoExecutable>())
    }

    init {
        config = configuration {
            net {
                this.port = this@MongoExecutable.port
                bindIp = "localhost"
            }
            processManagement {
                pidFilePath = File(baseDir, "${name}.pid").toString()
            }
            storage {
                dbPath = baseDir.getAbsolutePath()
                mmapv1 {
                    preallocDataFiles = false;
                }
            }
            systemLog {
                destination = Destination.FILE
                path = "${baseDir}/mongod.log"
            }
        }
    }

    fun isAlive(): Boolean {
        return process?.isAlive() ?: false;
    }

    fun clean() {
        shutdown()
        baseDir.deleteTree()
    }

    fun shutdown() {
        if (isAlive()) {
            LOG.info("Shutting down mongod on port ${port}")
            runCommand("db.shutdownServer()")
            process?.destroy(true)
        }
    }

    fun waitForStartUp() {
        val start = System.currentTimeMillis();
        var connected = tryConnect();
        while (!connected && System.currentTimeMillis() - start < 30000) {
            Thread.sleep(5000)
            connected = tryConnect()
        }
    }

    fun tryConnect(): Boolean {
        val stream = ByteArrayOutputStream()
        ProcessExecutor()
              .command(listOf(manager.mongo,
                    "admin", "--port", "${port}", "--quiet"))
              .redirectOutput(stream)
              .redirectError(Slf4jStream.of(LoggerFactory.getLogger(this.javaClass)).asInfo())
              .redirectInput(ByteArrayInputStream("db.stats()".toByteArray()))
              .execute()

        val json = String(stream.toByteArray()).trim()
        try {
            BsonDocumentCodec().decode(JsonReader(json), DecoderContext.builder().build())
            return true
        } catch(e: Exception) {
            return false
        }
    }

    public fun runCommandWithResult(command: String, authEnabled: Boolean = this.authEnabled, err: Logger = LOG):
           BsonDocument {
        val stream = ByteArrayOutputStream()
        val list = command(authEnabled)
        var commandString = ""
        if(authEnabled) {
            commandString = "db.auth(\"${MongoExecutable.SUPER_USER}\", \"${MongoExecutable.SUPER_USER_PASSWORD}\");\n"
        }
        commandString += command
        ProcessExecutor()
              .command(list)
              .redirectOutput(stream)
              .redirectError(Slf4jStream.of(err).asError())
              .redirectInput(ByteArrayInputStream(commandString.toByteArray()))
              .execute()

        val json = String(stream.toByteArray()).trim()
        try {
            return BsonDocumentCodec().decode(JsonReader(if (authEnabled) json.substring(2) else json), DecoderContext.builder().build())
        } catch(e: Exception) {
            println("failed to run '${command}' against server on port ${port}")
            println("json = ${json}")
            throw e;
        }
    }

    fun runCommand(command: String, authEnabled: Boolean = this.authEnabled, out: Logger = LOG, err: Logger = LOG) {
        val list = command(authEnabled)
        LOG.debug(list.join(" "))
        var commandString = ""
        if(authEnabled) {
            commandString = "db.auth(\"${MongoExecutable.SUPER_USER}\", \"${MongoExecutable.SUPER_USER_PASSWORD}\");\n"
        }
        commandString += command
        ProcessExecutor()
              .command(list)
              .redirectOutput(Slf4jStream.of(out).asDebug())
              .redirectError(Slf4jStream.of(err).asError())
              .redirectInput(ByteArrayInputStream(commandString.toByteArray()))
              .execute()
    }

    private fun command(authEnabled: Boolean): List<String> {
        val list = arrayListOf(manager.mongo,
              "admin", "--port", "${this.port}", "--quiet")
        if(authEnabled) {
            list.addAll(arrayOf("--username", MongoExecutable.SUPER_USER, "--password", MongoExecutable.SUPER_USER_PASSWORD,
                  "--authenticationDatabase", "admin"))
        }
        return list
    }
}


