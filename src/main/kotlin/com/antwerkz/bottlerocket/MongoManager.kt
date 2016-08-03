package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.executable.ConfigServer
import com.antwerkz.bottlerocket.executable.Mongod
import com.antwerkz.bottlerocket.executable.Mongos
import com.github.zafarkhaja.semver.Version
import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.ArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipUtils
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.SystemUtils
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.lang.String.format
import java.net.URL
import java.nio.file.Files
import java.util.zip.GZIPInputStream

class MongoManager(val versionManager: VersionManager) : VersionManager by versionManager {
    private val LOG = LoggerFactory.getLogger(MongoManager::class.java)

    companion object {
        @JvmStatic fun macDownload(version: Version): String {
            return "https://fastdl.mongodb.org/osx/mongodb-osx-x86_64-${version}.tgz"
        }
        @JvmStatic fun linuxDownload(version: Version): String {
            return "https://fastdl.mongodb.org/linux/mongodb-linux-x86_64-${version}.tgz"
        }
        @JvmStatic fun windowsDownload(version: Version): String {
            return "https://fastdl.mongodb.org/win32/mongodb-win32-x86_64-2008plus-${version}.zip"
        }
        @JvmStatic fun of(versionString: String): MongoManager {
            return MongoManager(BaseVersionManager.of(Version.valueOf(versionString)))
        }
    }

    val downloadPath: File
    val binDir: String
    var mongo: String

        get() = ""

        set(value) {}


    val mongod: String
    val mongos: String

    init {
        downloadPath = File(BottleRocket.TEMP_DIR, "mongo-downloads")
        binDir = "${download()}/bin"
        if (SystemUtils.IS_OS_WINDOWS) {
            mongo = "${binDir}/mongo.exe"
            mongod = "${binDir}/mongod.exe"
            mongos = "${binDir}/mongos.exe"
        } else {
            mongo = "${binDir}/mongo"
            mongod = "${binDir}/mongod"
            mongos = "${binDir}/mongos"
        }
    }

    fun download(): File {
        var url = if (SystemUtils.IS_OS_LINUX) {
            linuxDownload(version)
        } else if (SystemUtils.IS_OS_MAC_OSX) {
            macDownload(version)
        } else if (SystemUtils.IS_OS_WINDOWS) {
            windowsDownload(version)
        } else {
            throw RuntimeException("Unsupported operating system: ${SystemUtils.OS_NAME}")
        }

        return extractDownload({ downloadArchive(format(url, versionManager.version)) })
    }

    fun configServer(name: String, port: Int, baseDir: File): ConfigServer {
        return ConfigServer(this, name, port, baseDir)
    }

    fun mongod(name: String, port: Int, baseDir: File): Mongod {
        return Mongod(this, name, port, baseDir)
    }

    fun mongos(name: String, port: Int, baseDir: File, configServers: List<ConfigServer>): Mongos {
        return Mongos(this, name, port, baseDir, configServers)
    }

    fun extract(download: File): File {
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
        throw RuntimeException("Unsupported file type: ${download}")
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

    fun downloadArchive(path: String): File {
        try {
            val url = URL(path)
            var downloadName = url.path
            downloadName = downloadName.substring(downloadName.lastIndexOf('/') + 1)
            val download = File(downloadPath, downloadName)
            if (!download.exists()) {
                LOG.info("${download    } does not exist.  Downloading binaries from mongodb.org")
                download.parentFile.mkdirs()
                url.openConnection().inputStream.use { stream -> Files.copy(stream, download.toPath()) }
            }
            return download

        } catch (e: IOException) {
            throw RuntimeException(e.message, e)
        }

    }
}
