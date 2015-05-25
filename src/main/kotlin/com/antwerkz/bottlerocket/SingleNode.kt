package com.antwerkz.bottlerocket

import com.mongodb.ServerAddress
import org.apache.commons.lang3.SystemUtils
import org.slf4j.LoggerFactory
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.exec.StartedProcess
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream
import org.zeroturnaround.process.JavaProcess
import org.zeroturnaround.process.Processes
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.platform.platformStatic

open
public class SingleNode(name: String = DEFAULT_MONGOD_NAME, port: Int = DEFAULT_PORT,
                        version: String = DEFAULT_VERSION, baseDir: File = DEFAULT_BASE_DIR,
                        public val replSetName: String? = null) : MongoCluster(name, port, version, baseDir) {

    companion object {
        platformStatic public fun builder(): SingleNodeBuilder {
            return SingleNodeBuilder()
        }

        public fun configServer(): SingleNodeBuilder {
            return SingleNodeBuilder()
        }
    }

    private val mongod: Mongod = mongoManager.mongod(name, port, baseDir)

    public var replicaSet: ReplicaSet? = null

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

    fun isRunning(): Boolean {
        return mongod.isAlive();
    }

    override fun toString(): String {
        var content = "name = ${name}, port = ${port}, version = ${version}, baseDir = ${baseDir}";
        if (replSetName != null) {
            content += ", replSetName = ${replSetName}"
        }
        content += ", running = ${mongod.isAlive()}"
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