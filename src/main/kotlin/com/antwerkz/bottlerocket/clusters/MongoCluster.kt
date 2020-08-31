package com.antwerkz.bottlerocket.clusters

import com.antwerkz.bottlerocket.BottleRocket
import com.antwerkz.bottlerocket.DatabaseRole
import com.antwerkz.bottlerocket.MongoManager
import com.antwerkz.bottlerocket.configuration.Configuration
import com.antwerkz.bottlerocket.executable.MongoExecutable
import com.antwerkz.bottlerocket.executable.MongoExecutable.Companion.SUPER_USER_PASSWORD
import com.github.zafarkhaja.semver.Version
import com.mongodb.MongoClientSettings
import com.mongodb.MongoClientSettings.Builder
import com.mongodb.MongoCredential
import com.mongodb.MongoCredential.createCredential
import com.mongodb.ServerAddress
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
import java.security.Key
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.Security
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.Base64
import java.util.EnumSet
import java.util.concurrent.TimeUnit.SECONDS

abstract class MongoCluster(
    val clusterRoot: File = BottleRocket.DEFAULT_BASE_DIR,
    val name: String = BottleRocket.DEFAULT_NAME,
    val version: Version = BottleRocket.DEFAULT_VERSION,
    val allocator: PortAllocator = PortAllocator(BottleRocket.DEFAULT_PORT)
) : Configurable {
    companion object {
        val perms = EnumSet.of(OWNER_READ, OWNER_WRITE)
    }

    internal val logger: Logger = LoggerFactory.getLogger("${this::class.simpleName}-$name")
    internal val mongoManager: MongoManager = MongoManager(version)
    var adminAdded: Boolean = false
    val keyFile: String = File(clusterRoot, "rocket.key").absolutePath
    val pemFile: String = File(clusterRoot, "rocket.pem").absolutePath
    private var adminClient: MongoClient? = null
    private var client: MongoClient? = null
    private var credentials: MongoCredential? = null
    var configuration = Configuration()

    init {
        clusterRoot.mkdirs()
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
        mongoManager.deleteBinaries()
    }

    /*
        open fun enableAuth() {
            if (!isAuthEnabled()) {
                generateKeyFile()
                generatePemFile()
            }
        }
    */
    fun clean() {
        clusterRoot.deleteTree()
    }

    override fun configure(update: Configuration) {
        configuration = configuration.update(update)
    }

    fun getAdminClient(): MongoClient {
        if (adminClient == null) {
            val builder = MongoClientSettings.builder()
            configure(builder)
            if (isAuthEnabled()) {
                builder.credential(createCredential(MongoExecutable.SUPER_USER, "admin", SUPER_USER_PASSWORD.toCharArray()))
            }
            adminClient = MongoClients.create(builder.build())
        }

        return adminClient!!
    }

    @JvmOverloads
    fun getClient(builder: MongoClientSettings.Builder = MongoClientSettings.builder()): MongoClient {
        if (client == null) {
            configure(builder)
            credentials?.let {
                builder.credential(it)
            }
            client = MongoClients.create(builder.build())
        }

        return client!!
    }

    private fun configure(builder: Builder) {
        builder
            .applyToConnectionPoolSettings {
                it.maxWaitTime(30, SECONDS)
            }
            .applyToClusterSettings {
                it.hosts(getServerAddressList())
            }
    }

    fun generateKeyFile() {
        val key = File(keyFile)
        if (!key.exists()) {
            key.parentFile.mkdirs()
            val stream = FileOutputStream(key)
            stream.use {
                val secureRandom = SecureRandom()
                val bytes = ByteArray(741)
                secureRandom.nextBytes(bytes)
                stream.write(Base64.getEncoder().encode(bytes))
            }
        }
        try {
            Files.setPosixFilePermissions(key.toPath(), perms)
        } catch (ignored: UnsupportedOperationException) {
        }
    }

    fun pem() {
        val LOGGER = LoggerFactory.getLogger(MongoCluster::class.java)
        val KEY_SIZE = 1024
        fun generateRSAKeyPair(): KeyPair {
            val generator = KeyPairGenerator.getInstance("RSA", "BC")
            generator.initialize(KEY_SIZE)
            val keyPair = generator.generateKeyPair()
            LOGGER.info("RSA key pair generated.")
            return keyPair
        }

        fun writePemFile(key: Key, description: String, filename: String) {
            val pemFile = PemFile(key, description)
            pemFile.write(filename)
            LOGGER.info(String.format("%s successfully writen in file %s.", description, filename))
        }

        Security.addProvider(BouncyCastleProvider())
        LOGGER.info("BouncyCastle provider added.")
        val keyPair = generateRSAKeyPair()
        val priv = keyPair.private as RSAPrivateKey
        val pub = keyPair.public as RSAPublicKey
        writePemFile(priv, "RSA PRIVATE KEY", "id_rsa")
        writePemFile(pub, "RSA PUBLIC KEY", "id_rsa.pub")
    }

    fun generatePemFile() {
        val pem = File(pemFile)
        val keyFile = File(clusterRoot, "rocket-pem.key")
        val crtFile = File(clusterRoot, "rocket-pem.crt")
        if (!pem.exists()) {
            val generator = KeyPairGenerator.getInstance("RSA", "BC")
            generator.initialize(1204)
            val keyPair = generator.generateKeyPair()
//            ProcessExecutor()
//                    .directory(baseDir)
//                    .commandSplit("openssl req -batch -newkey rsa:2048 -new -x509 -days 365 -nodes -out ${crt.absolutePath} " +
//                            "-keyout ${key.absolutePath}")
//                    .redirectOutput(of(getLogger(javaClass)).asDebug())
//                    .redirectError(of(getLogger(javaClass)).asError())
//                    .execute()
            val pemStream = FileOutputStream(pem.absolutePath)
/*
            try {
                ProcessExecutor()
                        .directory(baseDir)
                        .commandSplit("cat ${key.absolutePath} ${crt.absolutePath}")
                        .redirectOutput(pemStream)
                        .redirectError(of(getLogger(javaClass)).asError())
                        .execute()
            } finally {
                pemStream.close()
            }
*/
        }
        try {
            Files.setPosixFilePermissions(pem.toPath(), perms)
            Files.setPosixFilePermissions(keyFile.toPath(), perms)
            Files.setPosixFilePermissions(crtFile.toPath(), perms)
        } catch (ignored: UnsupportedOperationException) {
        }
    }

    fun addUser(database: String, userName: String, password: String, roles: List<DatabaseRole>) {
        mongoManager.addUser(getAdminClient(), database, userName, password, roles)
        credentials = createCredential(userName, database, password.toCharArray())
    }

    fun versionAtLeast(minVersion: Version): Boolean {
        return version.greaterThanOrEqualTo(minVersion)
    }

    abstract fun isAuthEnabled(): Boolean
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
            throw RuntimeException("deleteTree() can only be called on directories:  $this")
        }
    }
}

abstract class MongoClusterBuilder<out T>() {
    var name: String = BottleRocket.DEFAULT_NAME
        private set
    var port: Int = BottleRocket.DEFAULT_PORT
        private set
    var version: Version = BottleRocket.DEFAULT_VERSION
        private set
    var baseDir: File = BottleRocket.DEFAULT_BASE_DIR
        private set

    open fun name(value: String): T {
        name = value
        baseDir = if (baseDir == BottleRocket.DEFAULT_BASE_DIR) File(
            "${BottleRocket.TEMP_DIR}/$name") else baseDir
        return this as T
    }

    fun port(value: Int): T {
        port = value
        return this as T
    }

    fun version(value: Version): T {
        version = value
        return this as T
    }

    fun baseDir(value: File): T {
        baseDir = value
        return this as T
    }
}