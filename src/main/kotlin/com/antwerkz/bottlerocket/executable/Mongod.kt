package com.antwerkz.bottlerocket.executable

import com.antwerkz.bottlerocket
import com.antwerkz.bottlerocket.MongoExecutable
import com.antwerkz.bottlerocket.MongoManager
import com.antwerkz.bottlerocket.ReplicaSet
import com.antwerkz.bottlerocket.configuration.State.ENABLED
import com.mongodb.ServerAddress
import org.slf4j.LoggerFactory
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream
import org.zeroturnaround.process.Processes
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream

public class Mongod(manager: MongoManager, name: String,
                    port: Int, baseDir: File, val replicaSet: ReplicaSet? = null) : MongoExecutable(manager, name, port, baseDir) {
    companion object {
        private val LOG = LoggerFactory.getLogger(javaClass<Mongod>())
    }

    init {
        configuration.merge(
              bottlerocket.configuration.configuration {
                  replication {
                      replSetName = replicaSet?.name
                  }
              }
        )
    }

    public fun start() {
        if (process == null || !process?.isAlive()!!) {
            baseDir.mkdirs()
            val config = File(baseDir, "mongod.conf")
            config.writeText(configuration.toYaml())

            val args = arrayListOf(manager.mongod,
                  "--config", config.getAbsolutePath())
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

    fun getServerAddress(): ServerAddress {
        return ServerAddress("localhost", port)
    }

    fun enableAuth(pemFile: String) {
    }
}