package com.antwerkz.bottlerocket

import com.mongodb.ServerAddress
import org.apache.commons.lang3.SystemUtils
import org.slf4j.LoggerFactory
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.exec.StartedProcess
import org.zeroturnaround.process.JavaProcess
import org.zeroturnaround.process.Processes
import java.io.File
import java.io.FileOutputStream

public class Mongod(public val name: String, public var port: Int,
                    public var version: String, public var  dataDir: File,
                    public var logDir: File, public val downloadManager: DownloadManager = DownloadManager()) : Commandable {

    companion object {
        private val LOG = LoggerFactory.getLogger(javaClass<Mongod>())
    }

    public var replSetName: String? = null

    public val mongod: String
    public val dbPath: File = File(dataDir, name)
    public val logPath: File = File(logDir, "${name}.log")
    public val binDir: String
    public val pidFile: File = File(dbPath, "${name}.pid")
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
            dbPath.mkdirs()
            logDir.mkdirs()

            println("Starting mongod with ${mongod}")
            val args = arrayListOf(mongod,
                  "--port", port.toString(),
                  "--logpath", logPath.toString(),
                  "--dbpath", dbPath.toString(),
                  "--pidfilepath", pidFile.toString(),
                  "-vvv"
            )
            if (replSetName != null) {
                args.addAll(array("--replSet", replSetName));
            }
            processResult = ProcessExecutor()
                  .command(args)
                  .redirectOutput(FileOutputStream(File(dbPath, "${name}.out")))
                  .redirectError(FileOutputStream(File(dbPath, "${name}.err")))
                  //              .redirectOutput(Slf4jStream.of(LoggerFactory.getLogger("Mongod.${name}")).asInfo())
                  //              .redirectError(Slf4jStream.of(LoggerFactory.getLogger("Mongod.${name}")).asInfo())
                  .destroyOnExit()
                  .start()
            process = Processes.newJavaProcess(processResult?.process());
        }
    }

    fun shutdown(): Boolean {
        LOG.info("Shutting down mongod on port ${port}")
        println("Shutting down mongod on port ${port}")
        val shutdown = runCommand(this, "db.shutdownServer()")
        process?.destroy(true)
        replicaSet?.waitForPrimary()
        return shutdown;
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