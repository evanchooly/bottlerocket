package com.antwerkz.bottlerocket

import java.io.File
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

val TEMP_DIR = if (File("/tmp").exists()) "/tmp" else System.getProperty("java.io.tmpdir")

public class MongoCluster(public var name: String = "BottleRocket",
                          public var basePort: Int = 30000,
                          public var version: String = "installed",
                          public var mongosCount: Int = -1,
                          public var replSetSize: Int = -1,
                          public var dataDir: File = File("${TEMP_DIR}/${name}/data"),
                          public var logDir: File = File("${TEMP_DIR}/${name}/logs")) {

    private val downloadManager: DownloadManager = DownloadManager()

    fun clean() {
        dataDir.deleteTree()
        logDir.deleteTree()
    }


    fun singleNode(): Mongod {
        return Mongod(name, basePort, version, dataDir, logDir)
    }

    fun replicaSet(replSetName: String = name): ReplicaSet {
        return ReplicaSet(this, replSetName, size = replSetSize)
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
