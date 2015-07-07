package com.antwerkz.bottlerocket.executable

import com.antwerkz.bottlerocket.MongoExecutable
import com.antwerkz.bottlerocket.MongoManager
import com.antwerkz.bottlerocket.configuration.State
import com.antwerkz.bottlerocket.configuration.configuration
import com.mongodb.ServerAddress
import org.slf4j.LoggerFactory
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream
import org.zeroturnaround.process.Processes
import java.io.File
import java.io.FileOutputStream

public class Mongod(manager: MongoManager, name: String,
                    port: Int, baseDir: File) : MongoExecutable(manager, name, port, baseDir) {
    companion object {
        private val LOG = LoggerFactory.getLogger(javaClass<Mongod>())
    }

    override val logger = LoggerFactory.getLogger("Mongod.${port}")

    public fun start() {
        if (process == null || !process?.isAlive()!!) {
            LOG.info("Starting mongod on port ${port}")
            baseDir.mkdirs()
            val configFile = File(baseDir, "mongod.conf")
            configFile.writeText(config.toYaml())

            val args = arrayListOf(manager.mongod,
                  "--config", configFile.getAbsolutePath())
            LOG.info("Starting mongod on port ${port}")
            var processResult = ProcessExecutor()
                  .command(args)
                  .redirectOutput(FileOutputStream(File(baseDir, "mongod.out")))
                  .redirectError(FileOutputStream(File(baseDir, "mongod.err")))
                  .destroyOnExit()
                  .start()
            process = Processes.newJavaProcess(processResult?.process())

            waitForStartUp()
        }
    }
}