package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.configuration.Configuration
import com.antwerkz.bottlerocket.configuration.mongo30.configuration
import com.antwerkz.bottlerocket.configuration.types.Destination.FILE
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
import java.util.stream.Stream
import java.util.zip.GZIPInputStream
import kotlin.platform.platformStatic

public class MongoManager(val versionManager: VersionManager) : VersionManager by versionManager {
    private val LOG = LoggerFactory.getLogger(javaClass<MongoManager>())

    public companion object {
        public var macDownload: String = "https://fastdl.mongodb.org/osx/mongodb-osx-x86_64-%s.tgz"
        public var linuxDownload: String = "https://fastdl.mongodb.org/linux/mongodb-linux-x86_64-%s.tgz"
        public var windowsDownload: String = "https://fastdl.mongodb.org/win32/mongodb-win32-x86_64-2008plus-%s.zip"

        platformStatic fun of(versionString: String): MongoManager {
            val version = Version.valueOf(versionString)
            return MongoManager(BaseVersionManager.of(version));
        }
    }

    public val downloadPath: File
    public val binDir: String
    public val mongo: String
    public val mongod: String
    public val mongos: String

    init {
        downloadPath = File(TEMP_DIR, "mongo-downloads")
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

    public fun download(): File {
        var url = if (SystemUtils.IS_OS_LINUX) {
            linuxDownload
        } else if (SystemUtils.IS_OS_MAC_OSX) {
            macDownload
        } else if (SystemUtils.IS_OS_WINDOWS) {
            windowsDownload
        } else {
            throw RuntimeException("Unsupported operating system: ${SystemUtils.OS_NAME}")
        }

        return extractDownload({ downloadArchive(format(url, versionManager.version)) })
    }

    public fun configServer(name: String, port: Int, baseDir: File): ConfigServer {
        return ConfigServer(this, name, port, baseDir)
    }

    public fun mongod(name: String, port: Int, baseDir: File): Mongod {
        return Mongod(this, name, port, baseDir)
    }

    public fun mongos(name: String, port: Int, baseDir: File, configServers: List<ConfigServer>): Mongos {
        return Mongos(this, name, port, baseDir, configServers)
    }

    public fun extract(download: File): File {
        if (GzipUtils.isCompressedFilename(download.getName())) {
            TarArchiveInputStream(GZIPInputStream(FileInputStream(download))).use { inputStream ->
                extract(inputStream)
            }
            return File(downloadPath, download.getName().substring(0, download.getName().length() - 4))
        } else if (download.getName().endsWith(".zip")) {
            try {
                ZipArchiveInputStream(FileInputStream(download)).use { inputStream ->
                    extract(inputStream)
                }
            } catch (e: IOException) {
                throw RuntimeException(e.getMessage(), e)
            }


            return File(downloadPath, download.getName().substring(0, download.getName().length() - 4))
        }
        throw RuntimeException("Unsupported file type: ${download}")
    }

    private fun extract(inputStream: ArchiveInputStream) {
        var entry: ArchiveEntry? = inputStream.getNextEntry()
        while (entry != null) {
            val file = File(downloadPath, entry.getName())
            file.getParentFile().mkdirs()
            val out = FileOutputStream(file)
            IOUtils.copy(inputStream, out)
            out.close()
            entry = inputStream.getNextEntry()
        }
    }

    private fun extractDownload(extractor: () -> File): File {
        var retry = 0
        while (true) {
            var download: File = extractor()
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
                    throw RuntimeException("Failed to extract file: ${e.getMessage()}")
                }
            }

        }
    }

    fun downloadArchive(path: String): File {
        try {
            val url = URL(path)
            var downloadName = url.getPath()
            downloadName = downloadName.substring(downloadName.lastIndexOf('/') + 1)
            val download = File(downloadPath, downloadName)
            if (!download.exists()) {
                LOG.info("${download    } does not exist.  Downloading binaries from mongodb.org")
                download.getParentFile().mkdirs()
                url.openConnection().getInputStream().use { stream -> Files.copy(stream, download.toPath()) }
            }
            return download

        } catch (e: IOException) {
            throw RuntimeException(e.getMessage(), e)
        }

    }
}
