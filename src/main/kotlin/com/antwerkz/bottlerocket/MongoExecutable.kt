package com.antwerkz.bottlerocket

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
    public var pidFile: File = File(baseDir, "${name}.pid")
    public var process: JavaProcess? = null
        protected set

    companion object {
        private val LOG = LoggerFactory.getLogger(javaClass<MongoExecutable>())
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
//        Awaitility.await()
//              .atMost(15, TimeUnit.SECONDS)
//              .pollInterval(500, TimeUnit.MILLISECONDS)
//              .until(tryConnect())
    }

    fun tryConnect(): Boolean {
        val stream = ByteArrayOutputStream()
        ProcessExecutor()
              .command(listOf(manager.mongo,
                    "admin", "--port", "${port}", "--quiet"))
              .redirectOutput(stream)
              .redirectError(Slf4jStream.of(LoggerFactory.getLogger(javaClass<ReplicaSet>())).asInfo())
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


    public fun start() {
        if (process == null || !process?.isAlive()!!) {
            baseDir.mkdirs()

            val args = arrayListOf(manager.mongod,
                  "--bind_ip", "localhost",
                  "--port", port.toString(),
                  "--logpath", "${baseDir}/mongod.log",
                  "--dbpath", baseDir.toString(),
                  "--pidfilepath", pidFile.toString())
            if (replicaSet != null) {
                args.addAll(listOf("--replSet", replicaSet.name))
            }
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

            LOG.info("Starting configsvr on port ${port}")
            var processResult = ProcessExecutor()
                  .command(manager.mongod,
                        "--configsvr",
                        "--port", port.toString(),
                        "--logpath", "${baseDir}/configsvr.log",
                        "--dbpath", baseDir.toString(),
                        "--pidfilepath", pidFile.toString()/*,
                        "-vvv"*/)
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
            pidFile = File(baseDir, "${name}.pid")

            LOG.info("Starting mongos on port ${port}")
            var processResult = ProcessExecutor()
                  .command(manager.mongos,
                        "--port", port.toString(),
                        "--logpath", "${baseDir}/mongos.log",
                        "--configdb", configServers.map { "localhost:${it.port}" }.join(","),
                        "--pidfilepath", pidFile.toString()/*,
                        "-vvv"*/
                  )
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