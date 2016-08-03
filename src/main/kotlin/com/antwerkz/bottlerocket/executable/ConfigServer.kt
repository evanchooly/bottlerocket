package com.antwerkz.bottlerocket.executable

import com.antwerkz.bottlerocket.MongoExecutable
import com.antwerkz.bottlerocket.MongoManager
import org.slf4j.LoggerFactory
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.process.Processes
import java.io.File
import java.io.FileOutputStream

class ConfigServer(manager: MongoManager, name: String,
                          port: Int, baseDir: File) : MongoExecutable(manager, name, port, baseDir) {
    companion object {
        private val LOG = LoggerFactory.getLogger(ConfigServer::class.java)
    }

    override val logger = LoggerFactory.getLogger("ConfigSvr.${port}")

    fun start() {
        if (process == null || !process?.isAlive!!) {
            baseDir.mkdirs()
            val file = File(baseDir, "configsvr.conf")

            manager.writeConfig(file, config)

            LOG.info("Starting configsvr on port ${port}")
            val processResult = ProcessExecutor()
                  .command(manager.mongod,
                        "--configsvr",
                        "--config", file.absolutePath)
                  .redirectOutput(FileOutputStream(File(baseDir, "configsvr.out")))
                  .redirectError(FileOutputStream(File(baseDir, "configsvr.err")))
                  //   .redirectOutput(Slf4jStream.of(LoggerFactory.getLogger("Mongod.${port}")).asInfo())
                  //   .redirectError(Slf4jStream.of(LoggerFactory.getLogger("Mongod.${port}")).asInfo())
                  .destroyOnExit()
                  .start()
            process = Processes.newPidProcess(processResult.process)

            waitForStartUp()
        } else {
            LOG.warn("start() was called on a running server: ${port}")
        }
    }
}