package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.clusters.MongoClusterBuilder
import com.antwerkz.bottlerocket.clusters.SingleNodeBuilder
import com.antwerkz.bottlerocket.configuration.mongo30.Configuration30
import com.antwerkz.bottlerocket.executable.Mongod
import com.mongodb.MongoClient
import com.mongodb.MongoClientOptions
import com.mongodb.ServerAddress
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.platform.platformStatic

open
public class SingleNode(name: String = DEFAULT_NAME, port: Int = DEFAULT_PORT, version: String = DEFAULT_VERSION,
                        baseDir: File = DEFAULT_BASE_DIR) : MongoCluster(name, port, version, baseDir) {

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
        super.shutdown()
        mongod.shutdown()
    }

    override fun isStarted(): Boolean {
        return mongod.isAlive()
    }

    override fun getServerAddressList(): List<ServerAddress> {
        return listOf(mongod.getServerAddress())
    }

    override
    fun startWithAuth() {
        if (!isAuthEnabled()) {
            start()
            if (!adminAdded) {
                mongoManager.addAdminUser(getAdminClient())
                adminAdded = true
            }
            shutdown()
            Thread.sleep(3000)
            mongoManager.enableAuth(mongod);
            super.startWithAuth()
        }
        start()
    }

    override fun isAuthEnabled(): Boolean {
        return mongod.isAuthEnabled()
    }

    override fun addUser(database: String, userName: String, password: String, roles: List<DatabaseRole>) {
        mongod.manager.addUser(getAdminClient(), database, userName, password, roles)
        super.addUser(database, userName, password, roles)
    }

    override fun updateConfig(update: Configuration30) {
        mongod.config.merge(update)
    }

    override fun allNodesActive() {
        if(!mongod.tryConnect()) {
            throw IllegalStateException("mongod:${port} is not active");
        }
    }

    override fun toString(): String {
        var content = "name = ${name}, version = ${version}, port = ${port}, baseDir = ${baseDir}, running = ${mongod.isAlive()}"
        if (isAuthEnabled()) {
            content += ", authentication = enabled"
        }
        return "Mongod { ${content} }"
    }
}