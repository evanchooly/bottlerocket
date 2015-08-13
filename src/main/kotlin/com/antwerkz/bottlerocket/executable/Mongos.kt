package com.antwerkz.bottlerocket.executable

import com.antwerkz.bottlerocket.MongoExecutable
import com.antwerkz.bottlerocket.MongoManager
import com.antwerkz.bottlerocket.configuration.ConfigMode.MONGOS
import org.slf4j.LoggerFactory
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.process.Processes
import java.io.File
import java.io.FileOutputStream

public class Mongos(manager: MongoManager, name: String, port: Int, baseDir: File, public val configServers: List<ConfigServer>)
: MongoExecutable(manager, name, port, baseDir) {

    companion object {
        private val LOG = LoggerFactory.getLogger(javaClass<Mongos>())
    }

    override val logger = LoggerFactory.getLogger("Mongos.${port}")

    fun start() {
        if (process == null || !process?.isAlive()!!) {
            baseDir.mkdirs()
            val file = File(baseDir, "mongos.conf")
            manager.writeConfig(file, config, MONGOS)

            LOG.info("Starting mongos on port ${port}")
            var processResult = ProcessExecutor()
                  .command(manager.mongos,
                        "--configdb", configServers.map { "localhost:${it.port}" }.join(","),
                        "--config", file.getAbsolutePath())
                  .redirectOutput(FileOutputStream(File(baseDir, "${name}.out")))
                  .redirectError(FileOutputStream(File(baseDir, "${name}.err")))
                  .destroyOnExit()
                  .start()
            process = Processes.newJavaProcess(processResult.process());

            waitForStartUp()
        }
    }
}