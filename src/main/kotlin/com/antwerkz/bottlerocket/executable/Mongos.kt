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