package com.antwerkz.bottlerocket

import org.slf4j.LoggerFactory
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.process.JavaProcess
import org.zeroturnaround.process.Processes
import java.io.File
import java.io.FileOutputStream
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
val DEFAULT_BASE_DIR = File("${TEMP_DIR}/${DEFAULT_MONGOD_NAME}")

public abstract class MongoCluster(public val name: String, public val port: Int, public val version: String, public val baseDir: File) {
    val mongoManager: MongoManager = MongoManager(version)

    public abstract fun start();

    public abstract fun shutdown();

    public open fun clean() {
        shutdown();
        baseDir.deleteTree()
    }
}

fun File.deleteTree() {
    if (exists()) {
        if (isDirectory()) {
            Files.walkFileTree(toPath(), object : SimpleFileVisitor<Path>() {
                override public fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

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
