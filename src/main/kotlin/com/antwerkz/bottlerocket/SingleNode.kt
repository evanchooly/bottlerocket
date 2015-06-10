package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.configuration.State.ENABLED
import com.antwerkz.bottlerocket.configuration.configuration
import com.antwerkz.bottlerocket.executable.Mongod
import com.mongodb.ServerAddress
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream
import java.io.ByteArrayInputStream
import java.io.File
import kotlin.platform.platformStatic

open
public class SingleNode(name: String = DEFAULT_MONGOD_NAME, port: Int = DEFAULT_PORT,
                        version: String = DEFAULT_VERSION, baseDir: File = DEFAULT_BASE_DIR,
                        public val replSetName: String? = null) : MongoCluster(name, port, version, baseDir) {

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(javaClass<SingleNode>())

        platformStatic public fun builder(): SingleNodeBuilder {
            return SingleNodeBuilder()
        }

        platformStatic fun builder(init: SingleNodeBuilder.() -> Unit): SingleNode {
            val builder = SingleNodeBuilder()
            builder.init()
            return builder().build()
        }
    }

    private val mongod: Mongod = mongoManager.mongod(name, port, baseDir)

    private var adminAdded: Boolean = false

    override
    fun start() {
        if (!mongod.isAlive()) {
            mongod.start()
        }
    }

    override
    fun shutdown() {
        mongod.shutdown()
    }

    override fun getServerAddressList(): List<ServerAddress> {
        return listOf(mongod.getServerAddress())
    }

    override
    fun enableAuth(pemFile: String) {
        if (!authEnabled()) {
            if (isRunning()) {
                shutdown()
                LOG.info("Waiting for running server on ${port} to shutdown before enabling authentication.")
                Thread.sleep(5000);
            }
            mongod.configuration.merge(
                  configuration {
                      security {
                          authorization = ENABLED
                      }
                  }
            )
            start()
            if (!adminAdded) {
                addAdmin()
                addRootUser()
                adminAdded = true
            }
            mongod.authEnabled = true
        }
    }


    private fun addAdmin() {
        var command = "db.createUser(\n" +
              "  {\n" +
              "    user: \"siteUserAdmin\",\n" +
              "    pwd: \"password\",\n" +
              "    roles: [ { role: \"userAdminAnyDatabase\", db: \"admin\" } ]\n" +
              "  })\n"
        ProcessExecutor()
              .command(mongoManager.mongo,
                    "--host", "localhost",
                    "--port", "${port}",
                    "admin")
              .redirectOutput(Slf4jStream.of(LoggerFactory.getLogger("Mongod.${port}")).asInfo())
              .redirectError(Slf4jStream.of(LoggerFactory.getLogger("Mongod.${port}")).asInfo())
              .redirectInput(ByteArrayInputStream(command.toByteArray()))
              .execute()
    }

    private fun addRootUser() {
        ProcessExecutor()
              .command(mongoManager.mongo,
                    "--host", "localhost",
                    "--port", "${port}",
                    "-u", "siteUserAdmin",
                    "-p", "password",
                    "admin")
              .redirectOutput(Slf4jStream.of(LoggerFactory.getLogger("Mongod.${port}")).asInfo())
              .redirectError(Slf4jStream.of(LoggerFactory.getLogger("Mongod.${port}")).asInfo())
              .redirectInput(ByteArrayInputStream(("db.createUser(\n" +
                    "  {\n" +
                    "    user: \"superuser\",\n" +
                    "    pwd: \"rocketman\",\n" +
                    "    roles: [ \"root\" ]\n" +
                    "  });")
                    .toByteArray()))
              .execute()
    }


    override fun authEnabled(): Boolean {
        return mongod.authEnabled
    }

    fun isRunning(): Boolean {
        return mongod.isAlive();
    }

    override fun toString(): String {
        var content = "name = ${name}, version = ${version}, port = ${port}, baseDir = ${baseDir}, running = ${mongod.isAlive()}"
        if (replSetName != null) {
            content += ", replSetName = ${replSetName}"
        }
        if (authEnabled()) {
            content += ", authentication = enabled"
        }
        return "Mongod { ${content} }"
    }
}

class SingleNodeBuilder() {
    public var name: String = DEFAULT_MONGOD_NAME
    public var port: Int = DEFAULT_PORT
    public var version: String = DEFAULT_VERSION
    public var baseDir: File = DEFAULT_BASE_DIR
    public var replSetName: String? = null;

    constructor(mongod: SingleNode) : this() {
        name = mongod.name
        port = mongod.port
        version = mongod.version
        baseDir = mongod.baseDir
        replSetName = mongod.replSetName
    }

    fun build(): SingleNode {
        return SingleNode(name, port, version, baseDir, replSetName)
    }
}