package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.configuration.ConfigMode.MONGOS
import com.antwerkz.bottlerocket.configuration.Configuration
import com.antwerkz.bottlerocket.configuration.Destination
import com.antwerkz.bottlerocket.configuration.configuration
import com.jayway.awaitility.Awaitility
import com.mongodb.ServerAddress
import org.bson.codecs.BsonDocumentCodec
import org.bson.codecs.DecoderContext
import org.bson.json.JsonReader
import org.slf4j.LoggerFactory
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream
import org.zeroturnaround.process.JavaProcess
import org.zeroturnaround.process.Processes
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

public open class MongoExecutable(val manager: MongoManager, val name: String, val port: Int, val baseDir: File) {
    public var process: JavaProcess? = null
        protected set
    val configuration: Configuration

    companion object {
        private val LOG = LoggerFactory.getLogger(javaClass<MongoExecutable>())
    }

    init {
        configuration = configuration {
            net {
                this.port = this@MongoExecutable.port
                bindIp = "localhost"
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

    fun isAlive(): Boolean {
        return process?.isAlive() ?: false;
    }

    fun clean() {
        shutdown()
        baseDir.deleteTree()
    }

    fun shutdown() {
        if (isAlive())
            LOG.info("Shutting down mongod on port ${port}")
        ProcessExecutor().command(manager.mongo,
              "admin", "--port", "${port}", "--quiet")
              .redirectOutput(Slf4jStream.of(LoggerFactory.getLogger(javaClass<MongoExecutable>())).asError())
              .redirectError(Slf4jStream.of(LoggerFactory.getLogger(javaClass<MongoExecutable>())).asError())
              .redirectInput(ByteArrayInputStream("db.shutdownServer()".toByteArray()))
              .execute()
        process?.destroy(true)
    }

    fun waitForStartUp() {
        val start = System.currentTimeMillis();
        var connected = tryConnect();
        while(!connected && System.currentTimeMillis() - start < 30000) {
            Thread.sleep(1000)
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
}


public class Mongod(manager: MongoManager, name: String,
                    port: Int, baseDir: File, val replicaSet: ReplicaSet? = null) : MongoExecutable(manager, name, port, baseDir) {
    companion object {
        private val LOG = LoggerFactory.getLogger(javaClass<Mongod>())
    }

    init {
        configuration.replication.replSetName = replicaSet?.name
    }

    public fun start() {
        if (process == null || !process?.isAlive()!!) {
            baseDir.mkdirs()
            val config = File(baseDir, "mongod.conf")
            config.writeText(configuration.toYaml())

            val args = arrayListOf(manager.mongod,
                  "--config", config.getAbsolutePath())
            LOG.info("Starting mongod on port ${port}")
            var processResult = ProcessExecutor()
                  .command(args)
                  .redirectOutput(FileOutputStream(File(baseDir, "mongod.out")))
                  .redirectError(FileOutputStream(File(baseDir, "mongod.err")))
                  //   .redirectOutput(Slf4jStream.of(LoggerFactory.getLogger("Mongod.${port}")).asInfo())
                  //   .redirectError(Slf4jStream.of(LoggerFactory.getLogger("Mongod.${port}")).asInfo())
                  .destroyOnExit()
                  .start()
            process = Processes.newJavaProcess(processResult?.process())

            waitForStartUp()
        }
    }

    fun getServerAddress(): ServerAddress {
        return ServerAddress("localhost", port)
    }

}

public class ConfigServer(manager: MongoManager, name: String,
                          port: Int, baseDir: File) : MongoExecutable(manager, name, port, baseDir) {
    companion object {
        private val LOG = LoggerFactory.getLogger(javaClass<Mongod>())
    }

    fun start() {
        if (process == null || !process?.isAlive()!!) {
            baseDir.mkdirs()
            val config = File(baseDir, "configsvr.conf")
            config.writeText(configuration.toYaml())

            val args = arrayListOf(manager.mongod,
                  "--config", config.getAbsolutePath())

            LOG.info("Starting configsvr on port ${port}")
            var processResult = ProcessExecutor()
                  .command(manager.mongod,
                        "--configsvr",
                        "--config", config.getAbsolutePath())
                  .redirectOutput(FileOutputStream(File(baseDir, "configsvr.out")))
                  .redirectError(FileOutputStream(File(baseDir, "configsvr.err")))
                  //   .redirectOutput(Slf4jStream.of(LoggerFactory.getLogger("Mongod.${port}")).asInfo())
                  //   .redirectError(Slf4jStream.of(LoggerFactory.getLogger("Mongod.${port}")).asInfo())
                  .destroyOnExit()
                  .start()
            process = Processes.newJavaProcess(processResult?.process())

            waitForStartUp()
        }
    }
}

public class Mongos(manager: MongoManager, name: String, port: Int, baseDir: File, public val configServers: List<ConfigServer>)
    : MongoExecutable(manager, name, port, baseDir) {

    companion object {
        private val LOG = LoggerFactory.getLogger(javaClass<Mongos>())
    }

    fun start() {
        if (process == null || !process?.isAlive()!!) {
            baseDir.mkdirs()
            val config = File(baseDir, "mongos.conf")
            config.writeText(configuration.toYaml(mode = MONGOS))

            LOG.info("Starting mongos on port ${port}")
            var processResult = ProcessExecutor()
                  .command(manager.mongos,
                        "--configdb", configServers.map { "localhost:${it.port}" }.join(","),
                        "--config", config.getAbsolutePath())
                  .redirectOutput(FileOutputStream(File(baseDir, "${name}.out")))
                  .redirectError(FileOutputStream(File(baseDir, "${name}.err")))
                  //                                .redirectOutput(Slf4jStream.of(LoggerFactory.getLogger("Mongod.${port}")).asInfo())
                  //                                .redirectError(Slf4jStream.of(LoggerFactory.getLogger("Mongod.${port}")).asInfo())
                  .destroyOnExit()
                  .start()
            process = Processes.newJavaProcess(processResult.process());

            waitForStartUp()
        }
    }
}