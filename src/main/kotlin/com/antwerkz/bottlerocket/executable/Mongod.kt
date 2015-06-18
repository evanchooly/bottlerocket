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

    val logger = LoggerFactory.getLogger("Mongod.${port}")

    public fun start() {
        if (process == null || !process?.isAlive()!!) {
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

    fun getServerAddress(): ServerAddress {
        return ServerAddress("localhost", port)
    }

    fun enableAuth(pemFile: String? = null) {
        config.merge(configuration {
            security {
//                authorization = if (pemFile == null) State.ENABLED else State.DISABLED
                authorization = State.ENABLED
                keyFile = pemFile
            }
        })
        authEnabled = true
    }

    fun addAdmin() {
        runCommand("db.createUser(\n" +
              "  {\n" +
              "    user: \"siteUserAdmin\",\n" +
              "    pwd: \"password\",\n" +
              "    roles: [ { role: \"userAdminAnyDatabase\", db: \"admin\" } ]\n" +
              "  })\n", out = logger, err = logger)
        authEnabled = true
    }

    fun addRootUser() {
        runCommand("db.createUser(\n" +
              "  {\n" +
              "    user: \"${MongoExecutable.SUPER_USER}\",\n" +
              "    pwd: \"${MongoExecutable.SUPER_USER_PASSWORD}\",\n" +
              "    roles: [ \"root\" ]\n" +
              "  });", out = logger, err = logger)
        authEnabled = true
    }
}