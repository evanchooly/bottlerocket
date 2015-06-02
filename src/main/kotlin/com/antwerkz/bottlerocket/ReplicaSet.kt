package com.antwerkz.bottlerocket

import com.jayway.awaitility.Awaitility
import com.mongodb.MongoClient
import com.mongodb.MongoClientOptions
import org.bson.BsonDocument
import org.bson.codecs.BsonDocumentCodec
import org.bson.codecs.DecoderContext
import org.bson.json.JsonReader
import org.slf4j.LoggerFactory
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.platform.platformStatic

class ReplicaSet(name: String, port: Int, version: String, public var size: Int, baseDir: File)
    : MongoCluster(name, port, version, baseDir) {

    public val nodes: MutableList<Mongod> = arrayListOf()
    private var nodeMap: Map<Int, Mongod> = hashMapOf()
    public var initialized: Boolean = false;
        private set
    private var client: MongoClient? = null;

    companion object {
        platformStatic fun builder(): ReplicaSetBuilder {
            return ReplicaSetBuilder()
        }
    }
    
    override
    fun start() {
        if (nodes.isEmpty()) {
            var port = port
            for ( i in 0..size - 1) {
                val nodeName = "${name}-${port}"
                nodes.add(mongoManager.mongod(nodeName, port, File(baseDir, nodeName), this))
                port += 1;
            }
        }
        for (node in nodes) {
            node.start()
        }
        nodeMap = nodes.toMap { it.port }

        initialize()
    }

    fun addNode(node: Mongod) {
        nodes.add(node);
    }

    private fun initialize() {
        if (initialized) {
            return;
        }
        val primary = nodes.first()

        initiateReplicaSet(primary)
        println("replSet initiated.  waiting for primary.")
        waitForPrimary()
        println("primary found.  adding other members.")
        nodes.asSequence().withIndex()
              .filter { it.index > 0 }
              .forEach { addMember(it.value) }

        waitForPrimary()
        initialized = true;

        println("replica set ${name} started.")
    }

    private fun initiateReplicaSet(mongod: Mongod) {

        val results = runCommand(mongod.port, "rs.initiate({"
              + "_id: \"${name}\","
              + "   members: [{"
              + "       _id: 1,"
              + "       host: \"localhost:${mongod.port}\""
              + "   }],"
              + "});")

        if ( !(results.getInt32("ok")?.intValue()?.equals(1) ?: false) ) {
            throw IllegalStateException("Failed to initiate replica set: ${results}")
        }
    }

    private fun addMember(mongod: Mongod) {
        val primary = getPrimary()
        if (primary == null) {
            throw IllegalStateException("Replica set ${name} has no primary")
        }
        val results = runCommand(primary.port, "rs.add(\"localhost:${mongod.port}\");")

        if ( results.getInt32("ok").getValue() != 1) {
            throw RuntimeException("Failed to add ${mongod} to replica set:  ${results}")
        }
    }

    fun getPrimary(): Mongod? {
        if (nodes.isEmpty()) {
            return null;
        }
        val mongod = nodes.filter({ it.isAlive() }).first()
        val result = runCommand(mongod.port, "db.isMaster()");

        if (result.containsKey ("primary")) {
            val host = result.getString("primary")!!.getValue()
            return nodeMap.get(Integer.valueOf(host.split(":".toRegex()).toTypedArray()[1]))
        } else {
            return null
        }
    }

    fun hasPrimary(): Boolean {
        return getPrimary() != null;
    }

    fun waitForPrimary(): Mongod? {
        Awaitility.await()
              .atMost(30, TimeUnit.SECONDS)
              .until({
                  try {
                      hasPrimary()
                  } catch(ignored: Exception) {

                  }
              })

        return getPrimary()
    }

    fun getClient(): MongoClient {
        if (client == null) {
            client = MongoClient(nodes.map { it.getServerAddress() },
                  MongoClientOptions.builder()
                        .connectTimeout(3000)
                        .build())
        }

        return client!!;
    }

    public fun runCommand(port: Int, command: String): BsonDocument {
        val stream = ByteArrayOutputStream()
        ProcessExecutor()
              .command(listOf(mongoManager.mongo,
                    "admin", "--port", "${port}", "--quiet"))
              .redirectOutput(stream)
              .redirectError(Slf4jStream.of(LoggerFactory.getLogger(javaClass<ReplicaSet>())).asInfo())
              .redirectInput(ByteArrayInputStream(command.toByteArray()))
              .execute()

        val json = String(stream.toByteArray()).trim()
        try {
            return BsonDocumentCodec().decode(JsonReader(json), DecoderContext.builder().build())
        } catch(e: Exception) {
            println("failed to run '${command}' against server on port ${port}")
            println("json = ${json}")
            throw e;
        }
    }

    override
    fun clean() {
        nodes.forEach { it.clean() }
        baseDir.deleteTree()
    }

    override
    fun shutdown() {
        client?.close()
        client = null;
        nodes.forEach { it.shutdown() }
    }

    fun url(): String {
        return "${name}/" + (nodes.map { "localhost:${it.port}"}.join(","))
    }
}

class ReplicaSetBuilder() {
    public var name: String = DEFAULT_MONGOD_NAME;
        set(value) {
            $name = value;
            baseDir = if (baseDir == DEFAULT_BASE_DIR) File("${TEMP_DIR}/${name}") else baseDir
        }
    public var basePort: Int = DEFAULT_PORT;
    public var version: String = DEFAULT_VERSION;
    public var size: Int = 3;
    public var baseDir: File = DEFAULT_BASE_DIR;
    
    fun build(): ReplicaSet {
        return ReplicaSet(name, basePort, version, size, baseDir)
    }
}