package com.antwerkz.bottlerocket

import java.io.File

val TEMP_DIR = if(File("/tmp").exists()) "/tmp" else System.getProperty("java.io.tmpdir")

public class MongoCluster(public var name: String = "BottleRocket",
                          public var basePort: Int = 30000,
                          public var version: String = "installed",
                          public var mongosCount: Int = -1,
                          public var replSetSize: Int = -1,
                          public var dataDir: File = File("${TEMP_DIR}/${name}/data"),
                          public var logDir: File = File("${TEMP_DIR}/${name}/logs")) {

    private val downloadManager: DownloadManager = DownloadManager()
    private var mongod: Mongod? = null

    fun singleNode(): Mongod {
        if (mongod == null) {
            mongod = Mongod("single", basePort, version, dataDir, logDir)
        };
        return mongod!!
    }

    fun replicaset(vararg names: String = array(), size: Int = 3): MongoCluster {
        return MongoCluster(replSetSize = size)
    }

    public fun create() {
    }
}
