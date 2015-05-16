package com.antwerkz.bottlerocket

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

open
public class Mongod(public val name: String = DEFAULT_MONGOD_NAME, public val port: Int = DEFAULT_PORT,
                    public val version: String = DEFAULT_VERSION, public val dbPath: File = DEFAULT_DBPATH,
                    public val logPath: File = DEFAULT_LOGPATH, public val replSetName: String? = null,
                    public val configServer: Boolean = false)
    : MongoCluster() {

    companion object {
        private val LOG = LoggerFactory.getLogger(javaClass<Mongod>())

        public fun builder(): MongodBuilder {
            return MongodBuilder()
        }
    }

    private val mongod: String
    public val binDir: String
    public var pidFile: File = File(dbPath, "${name}.pid")
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

    override
    fun start() {
        if (process == null || !process?.isAlive()!!) {
            dbPath.mkdirs()
            logPath.mkdirs()
            pidFile = File(dbPath, "${name}.pid")

            LOG.info("Starting mongod with ${mongod} on port ${port}")
            processResult = ProcessExecutor()
                  .command(buildArgs())
                  .redirectOutput(FileOutputStream(File(logPath, "${name}.out")))
                  .redirectError(FileOutputStream(File(logPath, "${name}.err")))
                  //                                .redirectOutput(Slf4jStream.of(LoggerFactory.getLogger("Mongod.${port}")).asInfo())
                  //                                .redirectError(Slf4jStream.of(LoggerFactory.getLogger("Mongod.${port}")).asInfo())
                  .destroyOnExit()
                  .start()
            process = Processes.newJavaProcess(processResult?.process());
        }
    }

    protected open fun buildArgs(): MutableList<String> {
        val args = arrayListOf(mongod,
              "--port", port.toString(),
              "--logpath", "${logPath}/mongod.log",
              "--dbpath", dbPath.toString(),
              "--pidfilepath", pidFile.toString(),
              "-vvv"
        )
        if (replSetName != null) {
            args.addAll(array("--replSet", replSetName));
        }
        return args
    }

    override
    fun shutdown() {
        LOG.info("Shutting down mongod on port ${port}")
        ProcessExecutor().command(listOf("${binDir}/${if (SystemUtils.IS_OS_WINDOWS) "mongo.exe" else "mongo"}",
              "admin", "--port", "${port}", "--quiet"))
              .redirectOutput(Slf4jStream.of(LoggerFactory.getLogger(javaClass<ReplicaSet>())).asError())
              .redirectError(Slf4jStream.of(LoggerFactory.getLogger(javaClass<ReplicaSet>())).asError())
              .redirectInput(ByteArrayInputStream("db.shutdownServer()".toByteArray()))
              .execute()
        process?.destroy(true)
    }

    fun getServerAddress(): ServerAddress {
        return ServerAddress("localhost", port)
    }

    fun isRunning(): Boolean {
        return process?.isAlive() ?: false;
    }

    override
    fun clean() {
        dbPath.deleteTree()
        logPath.deleteTree()
    }

    override fun toString(): String {
        var content = "name = ${name}, port = ${port}, version = ${version}, dataDir = ${dbPath}, logDir = ${logPath}";
        if (replSetName != null) {
            content += ", replSetName = ${replSetName}"
        }
        if (process != null) {
            content += ", running = ${process?.isAlive()}"
        }
        return "Mongod { ${content} }"
    }
}

class MongodBuilder() {
    public var name: String = DEFAULT_MONGOD_NAME
    public var port: Int = DEFAULT_PORT
    public var version: String = DEFAULT_VERSION
    public var dbPath: File = DEFAULT_DBPATH
    public var logPath: File = DEFAULT_LOGPATH
    public var replSetName: String? = null;

    constructor(mongod: Mongod) : this() {
        name = mongod.name
        port = mongod.port
        version = mongod.version
        dbPath = mongod.dbPath
        logPath = mongod.logPath
        replSetName = mongod.replSetName
    }
    
    fun mongod(): Mongod {
        return Mongod(name, port, version, dbPath, logPath, replSetName)
    }

    fun configServer(): Mongod {
        return ConfigServer(name, port, version, dbPath, logPath, replSetName)
    }
}

public class ConfigServer(name: String = DEFAULT_MONGOD_NAME, port: Int = DEFAULT_PORT,
                    version: String = DEFAULT_VERSION, dbPath: File = DEFAULT_DBPATH, logPath: File = DEFAULT_LOGPATH,
                    replSetName: String? = null) : Mongod(name, port, version, dbPath, logPath, replSetName) {
    override
    fun buildArgs(): MutableList<String> {
        val args = super.buildArgs();
        args.add("--configsvr")

        return args;
    }
}