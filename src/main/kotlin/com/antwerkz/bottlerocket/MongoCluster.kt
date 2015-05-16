package com.antwerkz.bottlerocket

import java.io.File
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

val TEMP_DIR = if (File("/tmp").exists()) "/tmp" else System.getProperty("java.io.tmpdir")

val DEFAULT_MONGOD_NAME = "rocket"
val DEFAULT_PORT = 30000
val DEFAULT_VERSION = "installed"
val DEFAULT_DBPATH = File("${TEMP_DIR}/${DEFAULT_MONGOD_NAME}/data")
val DEFAULT_LOGPATH = File("${TEMP_DIR}/${DEFAULT_MONGOD_NAME}/logs")
val DEFAULT_REPLSET_PATH = File("${TEMP_DIR}/${DEFAULT_MONGOD_NAME}")

public abstract class MongoCluster() : Commandable {

    public val downloadManager: DownloadManager = DownloadManager()

    abstract fun start();

    abstract fun shutdown();

    abstract fun clean();
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
