package com.antwerkz.bottlerocket.executable

import com.antwerkz.bottlerocket.MongoExecutable
import com.antwerkz.bottlerocket.MongoManager
import com.antwerkz.bottlerocket.configuration.ConfigMode.MONGOD
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream
import org.zeroturnaround.process.Processes
import java.io.File

class Mongod(manager: MongoManager, name: String, port: Int, baseDir: File) : MongoExecutable(manager, name, port, baseDir) {
    override val logger: Logger = LoggerFactory.getLogger("Mongod.$port")
    private val configFile = File(baseDir, "mongod.conf")
    fun start(replicaSetName: String? = null) {
        if (process == null || !process?.isAlive!!) {
            logger.info("Starting mongod on port $port")
            baseDir.mkdirs()
            val configFile = configFile
            manager.writeConfig(configFile, config, MONGOD)
            val args = arrayListOf(manager.mongod,
                "--config", configFile.absolutePath)
            if (replicaSetName != null) {
                args.addAll(arrayOf("--replSet", replicaSetName))
            }
            logger.debug("Starting mongod with this command: $args")
            val processResult = ProcessExecutor()
                .command(args)
                .redirectOutput(Slf4jStream.of(logger).asDebug())
                .redirectError(Slf4jStream.of(logger).asError())
                .destroyOnExit()
                .start()

            process = Processes.newPidProcess(processResult?.process)

            waitForStartUp()
        } else {
            logger.warn("start() was called on a running server: $port")
        }
    }
}