package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.configuration.Configuration
import com.github.zafarkhaja.semver.Version
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

public abstract class MongoCluster(public val name: String = BottleRocket.DEFAULT_NAME,
                                   public val port: Int = BottleRocket.DEFAULT_PORT,
                                   val version: String = BottleRocket.DEFAULT_VERSION,
                                   public val baseDir: File = BottleRocket.DEFAULT_BASE_DIR) {

    val mongoManager: MongoManager = MongoManager.of(version)
    var adminAdded: Boolean = false
    val keyFile: String = File(baseDir, "rocket.key").absolutePath
    val pemFile: String = File(baseDir, "rocket.pem").absolutePath

    private var adminClient: MongoClient? = null;
    private var client: MongoClient? = null;
    private var credentials = arrayListOf<MongoCredential>()

    init {
        baseDir.mkdirs()
    }

    abstract fun getServerAddressList(): List<ServerAddress>

    abstract fun isStarted(): Boolean

    fun restart() {
        shutdown()
        start()
    }

    abstract fun start();

    open fun shutdown() {
        adminClient?.close()
        adminClient = null;
        client?.close()
        client = null;
    }

    abstract fun isAuthEnabled(): Boolean;

    open fun enableAuth() {
        if (!isAuthEnabled()) {
            if (!adminAdded) {
                mongoManager.addAdminUser(getAdminClient())
                adminAdded = true
            }

            generateKeyFile()
            generatePemFile()
        }
    }

    fun clean() {
        baseDir.deleteTree()
    }

    fun getAdminClient(): MongoClient {
        if (adminClient == null) {
            val adminCredentials = arrayListOf<MongoCredential>()
            if(isAuthEnabled()) {
                adminCredentials.add(MongoCredential.createCredential(MongoExecutable.SUPER_USER, "admin",
                      MongoExecutable.SUPER_USER_PASSWORD.toCharArray()))
            }
            val builder = MongoClientOptions.builder()
                  .connectTimeout(3000)
            adminClient = MongoClient(getServerAddressList(), adminCredentials, builder.build())
        }

        return adminClient!!;
    }

    fun getClient(): MongoClient {
        if (client == null) {
            val builder = MongoClientOptions.builder()
                  .connectTimeout(3000)
            client = MongoClient(getServerAddressList(), credentials, builder.build())
        }

        return client!!;
    }

    fun generateKeyFile() {
        val key = File(keyFile)
        if (!key.exists()) {
            key.parentFile.mkdirs()
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
            var openssl = "openssl req -batch -newkey rsa:2048 -new -x509 -days 365 -nodes -out ${crt.absolutePath} -keyout ${key
                  .absolutePath}";
            var cat = "cat ${key.absolutePath} ${crt.absolutePath}"
            pem.parentFile.mkdirs()
            val stream = FileOutputStream(pem.absolutePath)
            ProcessExecutor()
                  .directory(baseDir)
                  .command(openssl.split(" "))
                  .redirectOutput(Slf4jStream.of(LoggerFactory.getLogger(javaClass)).asDebug())
                  .redirectError(Slf4jStream.of(LoggerFactory.getLogger(javaClass)).asError())
                  .execute()
            ProcessExecutor()
                  .directory(baseDir)
                  .command(cat.split(" "))
                  .redirectOutput(stream)
                  .redirectError(Slf4jStream.of(LoggerFactory.getLogger(javaClass)).asError())
                  .execute()
        }
        Files.setPosixFilePermissions(pem.toPath(), EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE))
        Files.setPosixFilePermissions(key.toPath(), EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE))
        Files.setPosixFilePermissions(crt.toPath(), EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE))
    }

    abstract fun updateConfig(update: Configuration)

    fun addUser(database: String, userName: String, password: String, roles: List<DatabaseRole>) {
        mongoManager.addUser(getAdminClient(), database, userName, password, roles)
        credentials.add(MongoCredential.createCredential(userName, database, password.toCharArray()))
    }

    fun versionAtLeast(minVersion: Version): Boolean {
        return Version.valueOf(version).greaterThanOrEqualTo(minVersion);
    }
}

fun File.deleteTree() {
    if (exists()) {
        if (isDirectory) {
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