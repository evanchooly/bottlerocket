package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.clusters.PortAllocator
import com.mongodb.ReadPreference
import com.mongodb.client.MongoClient
import org.bson.Document
import java.io.File

object BottleRocket {
    @JvmField
    val TEMP_DIR = System.getProperty("java.io.tmpdir")

    @JvmField
    var DEFAULT_NAME = "rocket"

    @JvmField
    var DEFAULT_PORT = 30000

    @JvmField
    var DEFAULT_VERSION = Versions.latest()

    @JvmField
    var DEFAULT_BASE_DIR = File(if (File("build").exists()) "build" else "target", DEFAULT_NAME)

    @JvmField
    var PORTS = PortAllocator(DEFAULT_PORT)
}

internal fun MongoClient.runCommand(command: String): Document {
    return getDatabase("admin")
        .runCommand(Document.parse(command))
}

internal fun MongoClient.runCommand(command: Document, readPreference: ReadPreference = ReadPreference.primary()): Document {
    return getDatabase("admin")
        .runCommand(command, readPreference)
}