package com.antwerkz.bottlerocket

import com.mongodb.MongoClient
import com.mongodb.MongoClientOptions
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import org.slf4j.LoggerFactory
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.PosixFilePermission
import java.util.EnumSet

val TEMP_DIR = if (File("/tmp").exists()) "/tmp" else System.getProperty("java.io.tmpdir")

val DEFAULT_NAME = "rocket"
val DEFAULT_PORT = 30000
val DEFAULT_VERSION = "installed"
val DEFAULT_BASE_DIR = File("${TEMP_DIR}/${DEFAULT_NAME}")

public abstract class MongoCluster(public val name: String = DEFAULT_NAME,
                                   public val port: Int = DEFAULT_PORT,
                                   public val version: String = DEFAULT_VERSION,
                                   public val baseDir: File = DEFAULT_BASE_DIR) {

    val mongoManager: MongoManager = MongoManager(version)
    private var client: MongoClient? = null;
    var adminAdded: Boolean = false
    val keyFile: String = File(baseDir, "rocket.key").getAbsolutePath()
    val pemFile: String = File(baseDir, "rocket.pem").getAbsolutePath()

    init {
        baseDir.mkdirs()
    }

    abstract fun start();

    open fun shutdown() {
        client?.close()
        client = null;
    }

    abstract fun isAuthEnabled(): Boolean;

    open fun enableAuth() {
        generateKeyFile()
        generatePemFile()
    }

    open fun clean() {
        shutdown();
        baseDir.deleteTree()
    }

    fun getClient(): MongoClient {
        if (client == null) {
            val builder = MongoClientOptions.builder()
                  .connectTimeout(3000)
            var credentials = if (isAuthEnabled()) {
                arrayListOf(MongoCredential.createCredential(MongoExecutable.SUPER_USER, "admin",
                      MongoExecutable.SUPER_USER_PASSWORD.toCharArray()))
            } else {
                listOf<MongoCredential>()
            }
            client = MongoClient(getServerAddressList(), credentials, builder.build())
        }

        return client!!;
    }

    abstract fun getServerAddressList(): List<ServerAddress>

    fun generateKeyFile() {
         val key = File(keyFile)
        if (!key.exists()) {
            key.getParentFile().mkdirs()
            val stream = FileOutputStream(key)
            try {
                ProcessExecutor()
                      .command(listOf("openssl", "rand", "-base64", "741"))
                      .redirectOutput(stream)
                      .redirectError(Slf4jStream.of(LoggerFactory.getLogger(javaClass)).asInfo())
                      .execute()
            } finally {
                stream.close()
            }
        }
        Files.setPosixFilePermissions(key.toPath(), EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE))
    }

    fun generatePemFile() {
        val pem = File(pemFile)
        val key = File(baseDir, "rocket-pem.key")
        val crt = File(baseDir, "rocket-pem.crt")
        if (!pem.exists()) {
            var openssl = "openssl req -batch -newkey rsa:2048 -new -x509 -days 365 -nodes -out ${crt.getAbsolutePath()} -keyout ${key
                  .getAbsolutePath()}";
            var cat = "cat ${key} ${crt}"
            pem.getParentFile().mkdirs()
            val stream = FileOutputStream(pem)
            ProcessExecutor()
                  .directory(baseDir)
                  .command(openssl.splitBy(" "))
                  .redirectOutputAsDebug()
                  .redirectError(Slf4jStream.of(LoggerFactory.getLogger(javaClass)).asError())
                  .execute()
            ProcessExecutor()
                  .directory(baseDir)
                  .command(cat.splitBy(" "))
                  .redirectOutput(stream)
                  .redirectError(Slf4jStream.of(LoggerFactory.getLogger(javaClass)).asError())
                  .execute()
        }
        Files.setPosixFilePermissions(pem.toPath(), EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE))
        Files.setPosixFilePermissions(key.toPath(), EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE))
        Files.setPosixFilePermissions(crt.toPath(), EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE))
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
