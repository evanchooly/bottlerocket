package com.antwerkz.bottlerocket.executable

import com.antwerkz.bottlerocket.MongoExecutable
import com.antwerkz.bottlerocket.MongoManager
import com.antwerkz.bottlerocket.configuration.ConfigMode.MONGOS
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.process.Processes
import java.io.File
import java.io.FileOutputStream

class Mongos(manager: MongoManager, name: String, port: Int, baseDir: File, val configServers: List<ConfigServer>)
    : MongoExecutable(manager, name, port, baseDir) {
    override val logger: Logger = LoggerFactory.getLogger("Mongos.$port")
    fun start() {
        if (process == null || !process?.isAlive!!) {
            baseDir.mkdirs()
            val file = File(baseDir, "mongos.conf")
            manager.writeConfig(file, config, MONGOS)

            logger.info("Starting mongos on port $port")
            val processResult = ProcessExecutor()
                .command(manager.mongos,
                    "--configdb", configServers.map { "localhost:${it.port}" }.joinToString(","),
                    "--config", file.absolutePath)
                .redirectOutput(FileOutputStream(File(baseDir, "$name.out")))
                .redirectError(FileOutputStream(File(baseDir, "$name.err")))
                .destroyOnExit()
                .start()
            process = Processes.newPidProcess(processResult.process)

            waitForStartUp()
        } else {
            logger.warn("start() was called on a running server: $port")
        }
    }
}