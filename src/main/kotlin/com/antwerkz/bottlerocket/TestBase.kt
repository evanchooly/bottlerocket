package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.clusters.SingleNode
import com.antwerkz.bottlerocket.configuration.types.Verbosity.FIVE
import com.github.zafarkhaja.semver.Version
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoDatabase
import org.bson.Document
import org.bson.UuidRepresentation.STANDARD
import org.testng.SkipException
import java.io.File

@Suppress("unused", "MemberVisibilityCanBePrivate")
open class TestBase {
    val mongoClient: MongoClient by lazy {
        startMongo()
    }
    val database: MongoDatabase by lazy {
        mongoClient.getDatabase(databaseName())
    }
    val serverVersion: Double = (mongoClient
        .getDatabase("admin")
        .runCommand(Document("serverStatus", 1))["version"] as String?)
        ?.substring(0, 3)?.toDouble()
        ?: throw IllegalStateException("Could not determine server version")

    open fun createCluster(version: Version) = SingleNode(File(dbPath()), databaseName(), version)
    open fun databaseName() = "mongoTest"
    open fun dbPath() = "target/mongo/"
    open fun uuidRepresentation() = STANDARD
    open fun version() = System.getenv("MONGODB")?.let { Version.valueOf(it) }
    fun isReplicaSet(): Boolean {
        return runIsMaster()["setName"] != null
    }

    fun checkMinServerVersion(version: Double) {
        if (serverVersion < version) {
            throw SkipException("Server should be at least $version but found $serverVersion")
        }
    }

    private fun runIsMaster(): Document {
        return mongoClient.getDatabase("admin")
            .runCommand(Document("ismaster", 1))
    }

    private fun startMongo(): MongoClient {
        val mongoClient: MongoClient
        val version = version()
        val builder = MongoClientSettings.builder()
        try {
            builder.uuidRepresentation(uuidRepresentation())
        } catch (ignored: Exception) {
            // not a 4.0 driver
        }
        if (version != null) {
            val cluster = createCluster(version)
            cluster.configure {
                systemLog {
                    traceAllExceptions = true
                    verbosity = FIVE
                }
            }
            cluster.clean()
            cluster.start()
            mongoClient = cluster.getClient(builder)
        } else {
            mongoClient = MongoClients.create(builder.build())
        }

        return mongoClient
    }
}