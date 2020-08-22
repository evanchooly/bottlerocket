package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.configuration.Configuration
import com.antwerkz.bottlerocket.configuration.configuration
import com.antwerkz.bottlerocket.executable.MongoExecutable
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
import java.lang.Thread.sleep
import java.net.URL
import java.util.Properties
import java.util.zip.GZIPInputStream

internal abstract class MongoManager(val version: Version, val windowsBaseUrl: String, val macBaseUrl: String, val linuxBaseUrl: String) {
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
                        val linux = Linux.get(props.getProperty("NAME") ?: "Fedora")

                        return linux.version(props)
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

        internal fun of(version: Version): MongoManager {
            return when ("${version.majorVersion}.${version.minorVersion}") {
                "4.4" -> MongoManager44(version)
                "4.2" -> MongoManager42(version)
                "4.0" -> MongoManager40(version)
                "3.6" -> MongoManager36(version)
                else -> throw IllegalArgumentException("Unsupported version $version")
            }
        }
        internal fun extension() = if (SystemUtils.IS_OS_WINDOWS) ".exe" else ""
    }

    internal lateinit var archive: File
    private val binDir: String by lazy { "${download()}/bin" }
    var downloadPath: File = File(BottleRocket.TEMP_DIR, "mongo-downloads")

    internal fun mongo() = "$binDir/mongo${extension()}"
    internal fun mongod() = "$binDir/mongod${extension()}"
    internal fun mongos() = "$binDir/mongos${extension()}"

    fun initialConfig(baseDir: File, name: String, port: Int): Configuration {
        return configuration {
            net {
                this.port = port
            }
            processManagement {
                pidFilePath = File(baseDir, "$name.pid").absolutePath
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

    internal fun mongod(baseDir: File, name: String, port: Int): Mongod {
        return Mongod(this, baseDir, name, port)
    }

    internal fun mongos(baseDir: File, name: String, port: Int): Mongos {
        return Mongos(this, baseDir, name, port)
    }

    internal fun macDownload(version: Version) = "$macBaseUrl$version.tgz"
    internal fun linuxDownload(version: Version) = "$linuxBaseUrl$version.tgz"
    internal fun windowsDownload(version: Version) = "$windowsBaseUrl$version.zip"

    private fun download(): File {
        val url = when {
            SystemUtils.IS_OS_LINUX -> linuxDownload(version)
            SystemUtils.IS_OS_MAC_OSX -> macDownload(version)
            SystemUtils.IS_OS_WINDOWS -> windowsDownload(version)
            else -> throw RuntimeException("Unsupported operating system: ${SystemUtils.OS_NAME}")
        }

        return extractDownload(format(url, version))
    }

    fun retry(count: Int, message: String, delay: Long = 1000, function: () -> Unit) {
        repeat(count) {
            try {
                return function()
            } catch (e: Throwable) {
                e.printStackTrace()
                sleep(delay)
            }
        }
        throw RuntimeException(message)
    }

    internal fun extractDownload(path: String): File {
        while (true) {
            var file: File? = null
            retry(5,  "Failed to extract file") {
                    try {
                        downloadArchive(path)
                        file = archive.extract()
                    } catch (e: IOException) {
                        LOG.error(e.message, e)
                        if (e.message?.contains("Truncated", true) ?: false) {
                            archive.delete()
                            downloadArchive(path)
                        }
                        sleep(1000)
                    }
                }

            file?.let {
                File(it, "bin").listFiles()
                    ?.forEach { file ->
                        file.setExecutable(true)
                    }
                return it
            }
        }
    }

    private fun File.extract(): File {
        val destination = File(parentFile, "mongo-$version")
        if (GzipUtils.isCompressedFilename(name)) {
            TarArchiveInputStream(GZIPInputStream(FileInputStream(this))).use { inputStream ->
                extract(inputStream, destination)
            }
        } else {
            ZipArchiveInputStream(FileInputStream(this)).use { inputStream ->
                extract(inputStream, destination)
            }
        }
        return destination
    }

    private fun extract(inputStream: ArchiveInputStream, destination: File) {
        var entry: ArchiveEntry? = inputStream.nextEntry
        while (entry != null) {
            val file = File(destination, entry.name.substringAfter("/"))
            file.parentFile.mkdirs()
            if (!file.exists() || entry.size != file.length()) {
                LOG.debug("Extracting archive entry to $file")
                FileOutputStream(file).use {
                    IOUtils.copy(inputStream, it)
                }
            }
            entry = inputStream.nextEntry
        }
    }

    internal fun downloadArchive(path: String) {
        retry(5, "Failed to download archive") {
            val url = URL(path)
            val downloadName = url.path.substringAfterLast('/')
            archive = File(downloadPath, downloadName)
            if (!archive.exists()) {
                LOG.info("$archive does not exist.  Downloading binaries from $url")
                archive.parentFile.mkdirs()
                Request.Get(path)
                    .userAgent("Mozilla/5.0 (compatible; bottlerocket; +https://github.com/evanchooly/bottlerocket)")
                    .execute()
                    .saveContent(archive)
            }
        }
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

internal class MongoManager36(version: Version) : MongoManager(
    version,
    linuxBaseUrl = "https://fastdl.mongodb.org/linux/mongodb-linux-x86_64-${linux()}-",
    macBaseUrl = "https://fastdl.mongodb.org/osx/mongodb-osx-ssl-x86_64-",
    windowsBaseUrl = "https://fastdl.mongodb.org/win32/mongodb-win32-x86_64-2008plus-ssl-"
) {
    companion object {
        fun linux() = MongoManager.linux().replace("1804", "1604")
    }
}

internal class MongoManager40(version: Version) : MongoManager(
    version,
    linuxBaseUrl = "https://fastdl.mongodb.org/linux/mongodb-linux-x86_64-${linux()}-",
    macBaseUrl = "https://fastdl.mongodb.org/osx/mongodb-osx-ssl-x86_64-",
    windowsBaseUrl = "https://fastdl.mongodb.org/win32/mongodb-win32-x86_64-2008plus-ssl-"
)

internal class MongoManager42(version: Version) : MongoManager(
    version,
    linuxBaseUrl = "https://fastdl.mongodb.org/linux/mongodb-linux-x86_64-${linux()}-",
    macBaseUrl = "https://fastdl.mongodb.org/osx/mongodb-macos-x86_64-",
    windowsBaseUrl = "https://fastdl.mongodb.org/win32/mongodb-win32-x86_64-2012plus-"
)

internal class MongoManager44(version: Version) : MongoManager(version,
    linuxBaseUrl = "https://fastdl.mongodb.org/linux/mongodb-linux-x86_64-${linux()}-",
    macBaseUrl = "https://fastdl.mongodb.org/osx/mongodb-macos-x86_64-",
    windowsBaseUrl = "https://fastdl.mongodb.org/windows/mongodb-windows-x86_64-"
)

@Suppress("unused")
enum class Linux {
    FEDORA {
        override fun version(props: Properties) = "rhel80"
    },
    UBUNTU {
        override fun version(props: Properties): String {
            val version = props.getProperty("VERSION_ID").replace(".", "").replace("\"", "")
            return "ubuntu" + when (version) {
                "1604", "1804" -> version
                else -> "1804"
            }
        }
    };

    abstract fun version(props: Properties): String

    companion object {
        internal fun get(name: String) = try {
            valueOf(name.toUpperCase())
        } catch (e: IllegalArgumentException) {
            FEDORA
        }
    }
}