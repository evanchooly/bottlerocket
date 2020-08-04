package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.configuration.ConfigMode
import com.antwerkz.bottlerocket.configuration.Configuration
import com.antwerkz.bottlerocket.configuration.configuration
import com.antwerkz.bottlerocket.configuration.managers.MongoManager36
import com.antwerkz.bottlerocket.configuration.managers.MongoManager40
import com.antwerkz.bottlerocket.configuration.managers.MongoManager42
import com.antwerkz.bottlerocket.configuration.managers.MongoManager44
import com.antwerkz.bottlerocket.executable.ConfigServer
import com.antwerkz.bottlerocket.executable.Mongod
import com.antwerkz.bottlerocket.executable.Mongos
import com.github.zafarkhaja.semver.Version
import com.mongodb.ReadPreference
import com.mongodb.client.MongoClient
import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.ArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipUtils
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.SystemUtils
import org.apache.http.client.fluent.Request
import org.bson.Document
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.String.format
import java.net.URL
import java.util.Properties
import java.util.zip.GZIPInputStream

abstract class MongoManager(val version: Version, val windowsBaseUrl: String, val macBaseUrl: String, val linuxBaseUrl: String) {
    companion object {
        private val LOG = LoggerFactory.getLogger(MongoManager::class.java)
        internal fun linux(): String {
            val etc = File("/etc")
            val version = when {
                File(etc, "redhat-release").exists() -> {
                    "rhel80"
                }
                File(etc, "os-release").exists() -> {
                    val props = Properties()
                    File(etc, "os-release").inputStream().use {
                        props.load(it)
                        val version = props["VERSION_ID"] as String

                        "ubuntu" + version.replace(".", "").replace("\"", "")
                    }
                }
                else -> {
                    "rhel80"
                }
            }
            if (SystemUtils.IS_OS_LINUX) {
                LOG.info("Linux version detected: $version")
            }
            return version
        }

        @JvmStatic
        fun of(versionString: String) = of(Version.valueOf(versionString))

        @JvmStatic
        fun of(version: Version): MongoManager {
            return when ("${version.majorVersion}.${version.minorVersion}") {
                "4.4" -> MongoManager44(version)
                "4.2" -> MongoManager42(version)
                "4.0" -> MongoManager40(version)
                "3.6" -> MongoManager36(version)
                else -> throw IllegalArgumentException("Unsupported version $version")
            }
        }
    }

    var archive: File? = null
    var downloadPath: File = File(BottleRocket.TEMP_DIR, "mongo-downloads")
        internal set(value) {
            field = value
        }
    val binDir: String by lazy { "${download()}/bin" }
    val mongo: String by lazy { "$binDir/mongo$extension" }
    val mongod: String by lazy { "$binDir/mongod$extension" }
    val mongos: String by lazy { "$binDir/mongos$extension" }
    private val extension = if (SystemUtils.IS_OS_WINDOWS) ".exe" else ""
    fun configServer(name: String, port: Int, baseDir: File): ConfigServer {
        return ConfigServer(this, name, port, baseDir)
    }

    fun writeConfig(configFile: File, config: Configuration, mode: ConfigMode) {
        configFile.writeText(config.toYaml(mode))
    }

    fun initialConfig(baseDir: File, name: String, port: Int): Configuration {
        return configuration {
            net {
                this.port = port
            }
            processManagement {
                pidFilePath = File(baseDir, "$name.pid").toString()
            }
            storage {
                dbPath = baseDir.absolutePath
            }
        }
    }

    fun deleteBinaries() {
        File(binDir).parentFile.deleteRecursively()
    }

    open fun getReplicaSetConfig(client: MongoClient): Document? {
        return client.getDatabase("local")
            .getCollection("system.replset").find().first()
    }

    /*
        fun enableAuth(node: MongoExecutable, pemFile: String? = null) {
            node.config.merge(configuration {
                security {
                    authorization = ENABLED
                    keyFile = pemFile
                }
            })
        }
    */
    fun addUser(client: MongoClient, database: String, userName: String, password: String, roles: List<DatabaseRole>) {
        if (!hasUser(client, database, userName)) {
            client.getDatabase(database).runCommand(Document("createUser", userName)
                .append("pwd", password)
                .append("roles", roles.map { it.toDB() }))
        }
    }

    private fun hasUser(client: MongoClient, database: String, userName: String): Boolean {
        val document = client.getDatabase(database)
            .runCommand(Document("usersInfo", userName))
        return (document["users"] as List<*>?)?.isNotEmpty() ?: false
    }

    fun addAdminUser(client: MongoClient) {
        addUser(client, "admin", MongoExecutable.SUPER_USER, MongoExecutable.SUPER_USER_PASSWORD,
            listOf(DatabaseRole("root", "admin"),
                DatabaseRole("userAdminAnyDatabase", "admin"),
                DatabaseRole("readWriteAnyDatabase", "admin")))
    }

    fun mongod(name: String, port: Int, baseDir: File): Mongod {
        return Mongod(this, name, port, baseDir)
    }

    fun mongos(name: String, port: Int, baseDir: File, configServers: List<ConfigServer>): Mongos {
        return Mongos(this, name, port, baseDir, configServers)
    }

    internal fun macDownload(version: Version): String {
        return "$macBaseUrl$version.tgz"
    }

    internal fun linuxDownload(version: Version): String {
        return "$linuxBaseUrl$version.tgz"
    }

    internal fun windowsDownload(version: Version): String {
        return "$windowsBaseUrl$version.zip"
    }

    private fun download(): File {
        val url = when {
            SystemUtils.IS_OS_LINUX -> linuxDownload(version)
            SystemUtils.IS_OS_MAC_OSX -> macDownload(version)
            SystemUtils.IS_OS_WINDOWS -> windowsDownload(version)
            else -> throw RuntimeException("Unsupported operating system: ${SystemUtils.OS_NAME}")
        }

        return extractDownload(format(url, version))
    }

    internal fun extractDownload(path: String): File {
        var retry = 0
        while (true) {
            archive = downloadArchive(path)
            try {
                val file = archive?.extract()
                file?.let {
                    File(it, "bin").listFiles()
                        ?.forEach { file ->
                            file.setExecutable(true)
                        }
                    return it
                }
            } catch (e: Throwable) {
                archive?.delete()
                retry++
                if (retry > 5) {
                    throw RuntimeException("Failed to extract file: ${e.message}", e)
                }
            }
        }
    }

    private fun File.extract(): File {
        val root = if (GzipUtils.isCompressedFilename(name)) {
            TarArchiveInputStream(GZIPInputStream(FileInputStream(this))).use { inputStream ->
                extract(inputStream)
            }
        } else {
            ZipArchiveInputStream(FileInputStream(this)).use { inputStream ->
                extract(inputStream)
            }
        }
        return File(downloadPath, root)
    }

    private fun extract(inputStream: ArchiveInputStream): String {
        var entry: ArchiveEntry? = inputStream.nextEntry
        var root: String? = null
        while (entry != null) {
            val file = File(downloadPath, entry.name)
            if (entry.name.contains("/")) {
                val base = entry.name.substringBefore("/")
                when (root) {
                    null -> root = base
                    else -> if (base != root) throw IllegalStateException("Found conflicting root folders:  $root vs $base")
                }
            }
            file.parentFile.mkdirs()
            val out = FileOutputStream(file)
            IOUtils.copy(inputStream, out)
            out.close()
            entry = inputStream.nextEntry
        }

        return root ?: throw IllegalStateException("No root folder found")
    }

    internal fun downloadArchive(path: String): File {
        val url = URL(path)
        val downloadName = url.path.substringAfterLast('/')
        val download = File(downloadPath, downloadName)
        if (!download.exists()) {
            LOG.info("$download does not exist.  Downloading binaries from $url")
            download.parentFile.mkdirs()
            Request.Get(path)
                .userAgent("Mozilla/5.0 (compatible; bottlerocket; +https://github.com/evanchooly/bottlerocket)")
                .execute()
                .saveContent(download)
        }
        return download
    }
}

fun MongoClient.runCommand(command: Document, readPreference: ReadPreference = ReadPreference.primary()): Document {
    try {
        return getDatabase("admin")
            .runCommand(command, readPreference)
    } catch (e: Exception) {
        throw RuntimeException("command failed: $command with preference $readPreference", e)
    }
}
