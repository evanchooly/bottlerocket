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

        platformStatic fun build(init: SingleNodeBuilder.() -> Unit): SingleNode {
            val builder = SingleNodeBuilder()
            builder.init()
            return builder().build()
        }
    }

    private val mongod: Mongod = mongoManager.mongod(name, port, baseDir)

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
    fun enableAuth() {
        if (!authEnabled()) {
            mongod.enableAuth()
            if (mongod.isAlive()) {
                shutdown()
                LOG.info("Waiting for running server on ${port} to shutdown before enabling authentication.")
                Thread.sleep(1000);
            }
            start()
            if (!adminAdded) {
//                mongod.addAdmin()
                mongod.addRootUser()
                adminAdded = true
            }
        }
    }

    override fun authEnabled(): Boolean {
        return mongod.authEnabled
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