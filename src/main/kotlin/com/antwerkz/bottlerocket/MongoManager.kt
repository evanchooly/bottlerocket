package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.configuration.ConfigMode
import com.antwerkz.bottlerocket.configuration.Configuration
import com.antwerkz.bottlerocket.configuration.configuration
import com.antwerkz.bottlerocket.configuration.mongo36.MongoManager36
import com.antwerkz.bottlerocket.configuration.mongo40.MongoManager40
import com.antwerkz.bottlerocket.configuration.mongo42.MongoManager42
import com.antwerkz.bottlerocket.configuration.types.Destination.FILE
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
import java.io.IOException
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
                File(etc, "os-release").exists() -> {
                    val props = Properties()
                    File(etc, "os-release").inputStream().use {
                        props.load(it)
                        val version = props["VERSION_ID"] as String

                        "ubuntu" + version.replace(".", "").replace("\"", "")
                    }
                }
                File(etc, "redhat-release").exists() -> {
                    "rhel80"
                }
                else -> {
                    "rhel80"
                }
            }
            LOG.info("Linux version detected: $version")
            return version
        }

        @JvmStatic
        fun of(versionString: String): MongoManager {
            val version = Version.valueOf(versionString)
            return when ("${version.majorVersion}.${version.minorVersion}") {
                "4.2" -> MongoManager42(version)
                "4.0" -> MongoManager40(version)
                "3.6" -> MongoManager36(version)
                else -> throw IllegalArgumentException("Unsupported version $version")
            }
        }

        @JvmStatic
        fun of(version: Version): MongoManager {
            return when ("${version.majorVersion}.${version.minorVersion}") {
                "4.2" -> MongoManager42(version)
                "4.0" -> MongoManager40(version)
                "3.6" -> MongoManager36(version)
                else -> throw IllegalArgumentException("Unsupported version $version")
            }
        }
    }

    val downloadPath: File
    val binDir: String
    var mongo: String
    val mongod: String
    val mongos: String

    init {
        downloadPath = File(BottleRocket.TEMP_DIR, "mongo-downloads")
        binDir = "${download()}/bin"
        if (SystemUtils.IS_OS_WINDOWS) {
            mongo = "$binDir/mongo.exe"
            mongod = "$binDir/mongod.exe"
            mongos = "$binDir/mongos.exe"
        } else {
            mongo = "$binDir/mongo"
            mongod = "$binDir/mongod"
            mongos = "$binDir/mongos"
        }
    }

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
            systemLog {
                destination = FILE
                path = "$baseDir/mongo.log"
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
        client.getDatabase(database).runCommand(Document("createUser", userName)
            .append("pwd", password)
            .append("roles", roles.map { it.toDB() }))
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

        return extractDownload { downloadArchive(format(url, version)) }
    }

    private fun extract(download: File): File {
        if (GzipUtils.isCompressedFilename(download.name)) {
            TarArchiveInputStream(GZIPInputStream(FileInputStream(download))).use { inputStream ->
                extract(inputStream)
            }
            return File(downloadPath, download.name.substring(0, download.name.length - 4))
        } else if (download.name.endsWith(".zip")) {
            try {
                ZipArchiveInputStream(FileInputStream(download)).use { inputStream ->
                    extract(inputStream)
                }
            } catch (e: IOException) {
                throw RuntimeException(e.message, e)
            }

            return File(downloadPath, download.name.substring(0, download.name.length - 4))
        }
        throw RuntimeException("Unsupported file type: $download")
    }

    private fun extract(inputStream: ArchiveInputStream) {
        var entry: ArchiveEntry? = inputStream.nextEntry
        while (entry != null) {
            val file = File(downloadPath, entry.name)
            file.parentFile.mkdirs()
            val out = FileOutputStream(file)
            IOUtils.copy(inputStream, out)
            out.close()
            entry = inputStream.nextEntry
        }
    }

    private fun extractDownload(extractor: () -> File): File {
        var retry = 0
        while (true) {
            val download: File = extractor()
            try {
                val file = extract(download)
                File(file, "bin").listFiles().forEach {
                    it.setExecutable(true)
                }

                return file
            } catch (e: Exception) {
                download.delete()
                retry++
                if (retry > 5) {
                    throw RuntimeException("Failed to extract file: ${e.message}")
                }
            }
        }
    }

    private fun downloadArchive(path: String): File {
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
