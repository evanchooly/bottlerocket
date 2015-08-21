package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.configuration.mongo30.Configuration30
import com.antwerkz.bottlerocket.configuration.mongo30.configuration
import com.antwerkz.bottlerocket.configuration.types.ClusterAuthMode
import com.antwerkz.bottlerocket.configuration.types.Compressor
import com.antwerkz.bottlerocket.configuration.types.Destination
import com.antwerkz.bottlerocket.configuration.types.IndexPrefetch
import com.antwerkz.bottlerocket.configuration.types.ProfilingMode
import com.antwerkz.bottlerocket.configuration.types.RotateBehavior
import com.antwerkz.bottlerocket.configuration.types.SslMode
import com.antwerkz.bottlerocket.configuration.types.State
import com.antwerkz.bottlerocket.configuration.types.TimestampFormat
import com.antwerkz.bottlerocket.configuration.types.Verbosity
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
val DEFAULT_VERSION = "3.0.5"
val DEFAULT_BASE_DIR = File("${TEMP_DIR}/${DEFAULT_NAME}")

public abstract class MongoCluster(public val name: String = DEFAULT_NAME,
                                   public val port: Int = DEFAULT_PORT,
                                   val version: String = DEFAULT_VERSION,
                                   public val baseDir: File = DEFAULT_BASE_DIR) {

    val mongoManager: MongoManager = MongoManager.of(version)
    var adminAdded: Boolean = false
    val keyFile: String = File(baseDir, "rocket.key").getAbsolutePath()
    val pemFile: String = File(baseDir, "rocket.pem").getAbsolutePath()

    private var adminClient: MongoClient? = null;
    private var client: MongoClient? = null;
    private var credentials = arrayListOf<MongoCredential>()

    init {
        baseDir.mkdirs()
    }

    abstract fun start();

    abstract fun isAuthEnabled(): Boolean;

    abstract fun isStarted(): Boolean

    abstract fun getServerAddressList(): List<ServerAddress>

    open fun shutdown() {
        adminClient?.close()
        adminClient = null;
        client?.close()
        client = null;
    }

    open fun startWithAuth() {
        generateKeyFile()
        generatePemFile()
    }

    open fun clean() {
        shutdown();
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
            var cat = "cat ${key.getAbsolutePath()} ${crt.getAbsolutePath()}"
            pem.getParentFile().mkdirs()
            val stream = FileOutputStream(pem.getAbsolutePath())
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

    abstract fun updateConfig(update: Configuration30)

    abstract fun allNodesActive()

    open fun addUser(database: String, userName: String, password: String, roles: List<DatabaseRole>) {
        credentials.add(MongoCredential.createCredential(userName, database, password.toCharArray()))
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