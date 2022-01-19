package com.antwerkz.bottlerocket.clusters

import com.antwerkz.bottlerocket.BottleRocket
import com.antwerkz.bottlerocket.configuration.Configuration
import com.antwerkz.bottlerocket.executable.Mongod
import com.github.zafarkhaja.semver.Version
import com.mongodb.ServerAddress
import java.io.File

class SingleNode @JvmOverloads constructor(
    clusterRoot: File = BottleRocket.DEFAULT_BASE_DIR,
    name: String = BottleRocket.DEFAULT_NAME,
    version: Version = BottleRocket.DEFAULT_VERSION,
    port: Int = BottleRocket.PORTS.next()
) : MongoCluster(clusterRoot, name, version) {

    private val mongod: Mongod = mongoManager.mongod(clusterRoot, name, port)

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

    override fun configure(update: Configuration) {
        mongod.configure(update)
    }

    override fun toString(): String {
        var content = "name = $name, version = $version, port = $allocator, baseDir = $clusterRoot, running = ${mongod.isAlive()}"
        if (isAuthEnabled()) {
            content += ", authentication = enabled"
        }
        return "Mongod { $content }"
    }
}