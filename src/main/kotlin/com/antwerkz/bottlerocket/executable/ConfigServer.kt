package com.antwerkz.bottlerocket.executable

import com.antwerkz.bottlerocket.MongoExecutable
import com.antwerkz.bottlerocket.MongoManager
import org.slf4j.LoggerFactory
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.process.Processes
import java.io.File
import java.io.FileOutputStream

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