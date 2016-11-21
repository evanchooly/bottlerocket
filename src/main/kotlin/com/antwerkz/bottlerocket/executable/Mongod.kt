package com.antwerkz.bottlerocket.executable

import com.antwerkz.bottlerocket.MongoExecutable
import com.antwerkz.bottlerocket.MongoManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.process.Processes
import java.io.File
import java.io.FileOutputStream

class Mongod(manager: MongoManager, name: String,
                    port: Int, baseDir: File) : MongoExecutable(manager, name, port, baseDir) {
    override val logger: Logger = LoggerFactory.getLogger("Mongod.${port}")
    private val stdOut: FileOutputStream by lazy {
        FileOutputStream(File(baseDir, "mongod.out"))
    }
    private val stdErr: FileOutputStream by lazy {
        FileOutputStream(File(baseDir, "mongod.err"))
    }
    private val configFile = File(baseDir, "mongod.conf")

    fun start(replicaSetName: String? = null) {
        if (process == null || !process?.isAlive!!) {
            logger.info("Starting mongod on port ${port}")
            baseDir.mkdirs()
            val configFile = configFile
            manager.writeConfig(configFile, config)

            val args = arrayListOf(manager.mongod,
                  "--config", configFile.absolutePath)
            if(replicaSetName != null) {
                args.addAll(arrayOf("--replSet", replicaSetName))
            }
            val processResult = ProcessExecutor()
                  .command(args)
                  .redirectOutput(stdOut)
                  .redirectError(stdErr)
                  .destroyOnExit()
                  .start()

            process = Processes.newPidProcess(processResult?.process)

            waitForStartUp()
        } else {
            logger.warn("start() was called on a running server: ${port}")
        }
    }
}