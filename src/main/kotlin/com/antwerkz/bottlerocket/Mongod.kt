package com.antwerkz.bottlerocket

import org.apache.commons.lang3.SystemUtils
import org.slf4j.LoggerFactory
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.exec.ProcessResult
import org.zeroturnaround.exec.StartedProcess
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream
import org.zeroturnaround.process.JavaProcess
import org.zeroturnaround.process.Processes
import java.io.File
import java.io.FileOutputStream

public class Mongod(public val name: String, public var port: Int,
                    public var version: String, public var  dataDir: File,
                    public var logDir: File) {

    private val downloadManager: DownloadManager = DownloadManager();
    private var mongod = ""

    public val dbPath: File = File(dataDir, name)
    public val logPath: File = File(logDir, "${name}.log")
    public val pidFile: File = File(dbPath, "${name}.pid")
    public var processResult: StartedProcess? = null

    private var process: JavaProcess? = null

    fun start() {
        dbPath.mkdirs()
        logDir.mkdirs()
        val file = downloadManager.download(version);
        mongod = "${file}/bin/${if (SystemUtils.IS_OS_WINDOWS) "mongod.exe" else "mongod"}"

        println("Starting mongod with ${mongod}")
        val args = listOf(mongod,
              "--port", port.toString(),
              "--logpath", logPath.toString(),
              "--dbpath", dbPath.toString(),
              "--pidfilepath", pidFile.toString()
        )
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

    fun shutdown(): Boolean {
        process?.destroy(false)
        return false;
    }
}