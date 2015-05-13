package com.antwerkz.bottlerocket

import com.fasterxml.jackson.databind.JsonNode
import com.mongodb.ServerAddress
import org.apache.commons.lang3.SystemUtils
import org.slf4j.LoggerFactory
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.exec.StartedProcess
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream
import org.zeroturnaround.process.JavaProcess
import org.zeroturnaround.process.Processes
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream

public class Mongod(name: String = DEFAULT_MONGOD_NAME, public var port: Int,
                    version: String) : MongoCluster(name, port, version) {

    companion object {
        private val LOG = LoggerFactory.getLogger(javaClass<Mongod>())
    }

    public var replSetName: String? = null

    public val mongod: String
    public val binDir: String
    public var pidFile: File = File(dataDir, "${name}.pid")
    public var processResult: StartedProcess? = null
        private set
    public var process: JavaProcess? = null
        private set
    public var replicaSet: ReplicaSet? = null

    init {
        val file = downloadManager.download(version);
        binDir = "${file}/bin"
        mongod = "${binDir}/${if (SystemUtils.IS_OS_WINDOWS) "mongod.exe" else "mongod"}"
    }

    fun start() {
        if (process == null || !process?.isAlive()!!) {
            dataDir.mkdirs()
            logDir.mkdirs()
            pidFile = File(dataDir, "${name}.pid")

            LOG.info("Starting mongod with ${mongod} on port ${port}")
            val args = arrayListOf(mongod,
                  "--port", port.toString(),
                  "--logpath", "${logDir}/mongod.log",
                  "--dbpath", dataDir.toString(),
                  "--pidfilepath", pidFile.toString(),
                  "-vvv"
            )
            if (replSetName != null) {
                args.addAll(array("--replSet", replSetName));
            }
            processResult = ProcessExecutor()
                  .command(args)
                  .redirectOutput(FileOutputStream(File(logDir, "${name}.out")))
                  .redirectError(FileOutputStream(File(logDir, "${name}.err")))
//                                .redirectOutput(Slf4jStream.of(LoggerFactory.getLogger("Mongod.${port}")).asInfo())
//                                .redirectError(Slf4jStream.of(LoggerFactory.getLogger("Mongod.${port}")).asInfo())
                  .destroyOnExit()
                  .start()
            process = Processes.newJavaProcess(processResult?.process());
        }
    }

    fun shutdown(): Boolean {
        LOG.info("Shutting down mongod on port ${port}")
        ProcessExecutor().command(listOf("${binDir}/${if (SystemUtils.IS_OS_WINDOWS) "mongo.exe" else "mongo"}",
                           "admin", "--port", "${port}", "--quiet"))
                     .redirectOutput(Slf4jStream.of(LoggerFactory.getLogger(javaClass<ReplicaSet>())).asError())
                     .redirectError(Slf4jStream.of(LoggerFactory.getLogger(javaClass<ReplicaSet>())).asError())
                     .redirectInput(ByteArrayInputStream("db.shutdownServer()".toByteArray()))
                     .execute()
        process?.destroy(true)
        return process?.isAlive() ?: false;
    }

    fun getServerAddress(): ServerAddress {
        return ServerAddress("localhost", port)
    }

    fun isRunning(): Boolean {
        return process?.isAlive() ?: false;
    }

    override fun toString(): String {
        var content = "name = ${name}, port = ${port}, version = ${version}, dataDir = ${dataDir}, logDir = ${logDir}";
        if (replSetName != null) {
            content += ", replSetName = ${replSetName}"
        }
        if (process != null) {
            content += ", running = ${process?.isAlive()}"
        }
        return "Mongod { ${content} }"
    }
}