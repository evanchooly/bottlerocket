package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.clusters.SingleNodeBuilder
import com.antwerkz.bottlerocket.configuration.Configuration
import com.antwerkz.bottlerocket.executable.Mongod
import com.mongodb.ServerAddress
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

open
public class SingleNode(name: String = BottleRocket.DEFAULT_NAME,
                        port: Int = BottleRocket.DEFAULT_PORT,
                        version: String = BottleRocket.DEFAULT_VERSION,
                        baseDir: File = BottleRocket.DEFAULT_BASE_DIR) :
        MongoCluster(name, port, version, baseDir) {

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(SingleNode::class.java)

        @JvmStatic public fun builder(): SingleNodeBuilder {
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
//            Thread.sleep(3000)
        }
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

    override
    fun enableAuth() {
        super.enableAuth()
        mongoManager.enableAuth(mongod);
    }

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