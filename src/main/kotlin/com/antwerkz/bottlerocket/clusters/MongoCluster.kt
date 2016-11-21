package com.antwerkz.bottlerocket.clusters

import com.antwerkz.bottlerocket.BottleRocket
import com.antwerkz.bottlerocket.DatabaseRole
import com.antwerkz.bottlerocket.MongoExecutable
import com.antwerkz.bottlerocket.MongoManager
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
import java.nio.file.FileVisitResult.CONTINUE
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.PosixFilePermission.OWNER_READ
import java.nio.file.attribute.PosixFilePermission.OWNER_WRITE
import java.util.EnumSet

abstract class MongoCluster(val name: String = BottleRocket.DEFAULT_NAME,
                            val port: Int = BottleRocket.DEFAULT_PORT,
                            val version: String = BottleRocket.DEFAULT_VERSION,
                            val baseDir: File = BottleRocket.DEFAULT_BASE_DIR) {

    companion object {
        val perms = EnumSet.of(OWNER_READ, OWNER_WRITE)
    }

    val mongoManager: MongoManager = MongoManager.of(version)
    var adminAdded: Boolean = false
    val keyFile: String = File(baseDir, "rocket.key").absolutePath
    val pemFile: String = File(baseDir, "rocket.pem").absolutePath

    private var adminClient: MongoClient? = null
    private var client: MongoClient? = null
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

    open fun start() {
        if (!adminAdded) {
            mongoManager.addAdminUser(getAdminClient())
            adminAdded = true
        }

    }

    open fun shutdown() {
        adminClient?.close()
        adminClient = null
        client?.close()
        client = null
    }

    open fun enableAuth() {
        if (!isAuthEnabled()) {
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
            if (isAuthEnabled()) {
                adminCredentials.add(MongoCredential.createCredential(MongoExecutable.SUPER_USER, "admin",
                        MongoExecutable.SUPER_USER_PASSWORD.toCharArray()))
            }
            val builder = MongoClientOptions.builder()
                  .connectTimeout(3000)
            adminClient = MongoClient(getServerAddressList(), adminCredentials, builder.build())
        }

        return adminClient!!
    }

    fun getClient(): MongoClient {
        if (client == null) {
            val builder = MongoClientOptions.builder()
                  .connectTimeout(3000)
            client = MongoClient(getServerAddressList(), credentials, builder.build())
        }

        return client!!
    }

    fun generateKeyFile() {
        val key = File(keyFile)
        if (!key.exists()) {
            key.parentFile.mkdirs()
            val stream = FileOutputStream(key)
            try {
                ProcessExecutor()
                        .commandSplit("openssl rand -base64 741")
                        .redirectOutput(stream)
                        .redirectError(Slf4jStream.of(LoggerFactory.getLogger(javaClass)).asError())
                        .execute()
            } finally {
                stream.close()
            }
        }
        try {
            Files.setPosixFilePermissions(key.toPath(), perms)
        } catch(ignored : UnsupportedOperationException) {

        }
    }

    fun generatePemFile() {
        val pem = File(pemFile)
        val key = File(baseDir, "rocket-pem.key")
        val crt = File(baseDir, "rocket-pem.crt")
        if (!pem.exists()) {
            pem.parentFile.mkdirs()
            ProcessExecutor()
                    .directory(baseDir)
                    .commandSplit("openssl req -batch -newkey rsa:2048 -new -x509 -days 365 -nodes -out ${crt.absolutePath} " +
                            "-keyout ${key.absolutePath}")
                    .redirectOutput(Slf4jStream.of(LoggerFactory.getLogger(javaClass)).asDebug())
                    .redirectError(Slf4jStream.of(LoggerFactory.getLogger(javaClass)).asError())
                    .execute()
            val pemStream = FileOutputStream(pem.absolutePath)
            try {
                ProcessExecutor()
                        .directory(baseDir)
                        .commandSplit("cat ${key.absolutePath} ${crt.absolutePath}")
                        .redirectOutput(pemStream)
                        .redirectError(Slf4jStream.of(LoggerFactory.getLogger(javaClass)).asError())
                        .execute()
            } finally {
                pemStream.close()
            }
        }
        try {
            Files.setPosixFilePermissions(pem.toPath(), perms)
            Files.setPosixFilePermissions(key.toPath(), perms)
            Files.setPosixFilePermissions(crt.toPath(), perms)
        } catch(ignored : UnsupportedOperationException) {
        }
    }

    fun addUser(database: String, userName: String, password: String, roles: List<DatabaseRole>) {
        mongoManager.addUser(getAdminClient(), database, userName, password, roles)
        credentials.add(MongoCredential.createCredential(userName, database, password.toCharArray()))
    }

    fun versionAtLeast(minVersion: Version): Boolean {
        return Version.valueOf(version).greaterThanOrEqualTo(minVersion)
    }

    abstract fun isAuthEnabled(): Boolean

    abstract fun updateConfig(update: Configuration)

}

fun File.deleteTree() {
    if (exists()) {
        if (isDirectory) {
            Files.walkFileTree(toPath(), object : SimpleFileVisitor<Path>() {
                override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                    Files.delete(file)
                    return CONTINUE
                }

                override fun postVisitDirectory(dir: Path, e: IOException?): FileVisitResult {
                    if (e == null) {
                        Files.delete(dir)
                        return CONTINUE
                    } else {
                        // directory iteration failed
                        throw e
                    }
                }
            })
        } else {
            throw RuntimeException("deleteTree() can only be called on directories:  ${this}")
        }
    }
}

abstract class MongoClusterBuilder<out T>() {
    var name: String = BottleRocket.DEFAULT_NAME
        private set
    var port: Int = BottleRocket.DEFAULT_PORT
        private set
    var version: String = BottleRocket.DEFAULT_VERSION
        private set
    var baseDir: File = BottleRocket.DEFAULT_BASE_DIR
        private set

    open fun name(value: String): T {
        name = value
        baseDir = if (baseDir == BottleRocket.DEFAULT_BASE_DIR) File(
              "${BottleRocket.TEMP_DIR}/${name}") else baseDir
        return this as T
    }

    fun port(value: Int): T {
        port = value
        return this as T
    }

    fun version(value: String): T {
        version = value
        return this as T
    }

    fun baseDir(value: File): T {
        baseDir = value
        return this as T
    }
}