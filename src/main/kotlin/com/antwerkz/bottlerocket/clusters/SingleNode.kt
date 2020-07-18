package com.antwerkz.bottlerocket.clusters

import com.antwerkz.bottlerocket.BottleRocket
import com.antwerkz.bottlerocket.configuration.Configuration
import com.antwerkz.bottlerocket.executable.Mongod
import com.mongodb.ServerAddress
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

open class SingleNode @JvmOverloads constructor(name: String = BottleRocket.DEFAULT_NAME,
                      port: Int = BottleRocket.DEFAULT_PORT,
                      version: String = BottleRocket.DEFAULT_VERSION,
                      baseDir: File = BottleRocket.DEFAULT_BASE_DIR) :
        MongoCluster(name, port, version, baseDir) {

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(SingleNode::class.java)

        @JvmStatic fun builder(): SingleNodeBuilder {
            return SingleNodeBuilder()
        }

        @JvmStatic fun build(init: SingleNodeBuilder.() -> Unit): SingleNode {
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
        super.start()
    }

    override
    fun shutdown() {
        mongod.shutdown()
        super.shutdown()
    }

    override fun isStarted(): Boolean {
        return mongod.isAlive()
    }

    override fun getServerAddressList(): List<ServerAddress> {
        return listOf(mongod.getServerAddress())
    }

/*
    override
    fun enableAuth() {
        super.enableAuth()
        mongoManager.enableAuth(mongod)
    }
*/

    override fun isAuthEnabled(): Boolean {
        return mongod.isAuthEnabled()
    }

    override fun updateConfig(update: Configuration) {
        mongod.config.merge(update)
    }

    override fun toString(): String {
        var content = "name = ${name}, version = ${version}, port = ${port}, baseDir = ${baseDir}, running = ${mongod.isAlive()}"
        if (isAuthEnabled()) {
            content += ", authentication = enabled"
        }
        return "Mongod { ${content} }"
    }
}

class SingleNodeBuilder() : MongoClusterBuilder<SingleNodeBuilder>() {
    constructor(mongod: SingleNode) : this() {
        name(mongod.name)
        port(mongod.port)
        version(mongod.version)
        baseDir(mongod.baseDir)
    }

    fun build(): SingleNode {
        return SingleNode(name, port, version, baseDir)
    }
}