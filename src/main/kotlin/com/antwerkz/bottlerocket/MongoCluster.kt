package com.antwerkz.bottlerocket

import java.io.File
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

val TEMP_DIR = if (File("/tmp").exists()) "/tmp" else System.getProperty("java.io.tmpdir")
val DEFAULT_MONGOD_NAME = "BottleRocket"
val DEFAULT_PORT = 30000
val DEFAULT_VERSION = "installed"

open public class MongoCluster(public var name: String = DEFAULT_MONGOD_NAME,
                          public var basePort: Int = DEFAULT_PORT,
                          public var version: String = DEFAULT_VERSION) : Commandable {

    public val downloadManager: DownloadManager = DownloadManager()

    companion object {
        fun singleNode(name: String = DEFAULT_MONGOD_NAME, port: Int = DEFAULT_PORT, version: String = DEFAULT_VERSION): Mongod {
            return Mongod(name, port, version)
        }

        fun replicaSet(name: String = DEFAULT_MONGOD_NAME, port: Int = DEFAULT_PORT, version: String = DEFAULT_VERSION,
                       size: Int = 3): ReplicaSet {
            return ReplicaSet(name, port, version, size)
        }
    }

    public var dataDir: File = File("${TEMP_DIR}/${name}/data")
    public var logDir: File = File("${TEMP_DIR}/${name}/logs")

    fun clean() {
        dataDir.deleteTree()
        logDir.deleteTree()
    }


}

fun File.deleteTree() {
    if (exists()) {
        if (isDirectory()) {
            Files.walkFileTree(toPath(), object : SimpleFileVisitor<Path>() {
                throws(javaClass<IOException>())
                override public fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                throws(javaClass<IOException>())
                override public fun postVisitDirectory(dir: Path, e: IOException?): FileVisitResult {
                    if (e == null) {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    } else {
                        // directory iteration failed
                        throw e;
                    }
                }
            });
        } else {
            throw RuntimeException("deleteTree() can only be called on directories:  ${this}")
        }
    }
}
