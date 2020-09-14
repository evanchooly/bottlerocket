package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.LinuxDistribution.TestDistro
import com.github.zafarkhaja.semver.Version
import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.ArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipUtils
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.SystemUtils
import org.apache.http.client.fluent.Request
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.util.zip.GZIPInputStream

sealed class MongoDistribution(val version: Version) {
    companion object {
        private val LOG = LoggerFactory.getLogger(MongoDistribution::class.java)
        internal fun of(version: Version): MongoDistribution {
            return when ("${version.majorVersion}.${version.minorVersion}") {
                "4.4" -> MongoDistribution44(version)
                "4.2" -> MongoDistribution42(version)
                "4.0" -> MongoDistribution40(version)
                "3.6" -> MongoDistribution36(version)
                else -> throw IllegalArgumentException("Unsupported version $version")
            }
        }
    }

    internal lateinit var archive: File
    internal val binDir: String by lazy { "${download()}/bin" }
    internal var downloadPath: File = File(BottleRocket.TEMP_DIR, "mongo-downloads")
    internal open fun macDownload(version: Version) = "https://fastdl.mongodb.org/osx/mongodb-macos-x86_64-$version.tgz"
    internal open fun linuxDownload(version: Version) = "https://fastdl.mongodb.org/linux/mongodb-linux-x86_64-${linux()}-$version.tgz"
    internal abstract fun windowsDownload(version: Version): String
    internal var linux: LinuxDistribution = TestDistro("test", "...", "...")
        get() {
            if (field is TestDistro) {
                field = LinuxDistribution.parse(File("/etc/os-release"))
            }
            return field
        }

    internal open fun linux(): String {
        LOG.debug("Linux distribution detected: $linux")
        return linux.mongoVersion()
    }

    private fun download(): File {
        val url = when {
            SystemUtils.IS_OS_LINUX -> linuxDownload(version)
            SystemUtils.IS_OS_MAC_OSX -> macDownload(version)
            SystemUtils.IS_OS_WINDOWS -> windowsDownload(version)
            else -> throw RuntimeException("Unsupported operating system: ${SystemUtils.OS_NAME}")
        }

        return extractDownload(String.format(url, version))
    }

    fun retry(count: Int, delay: Long = 1000, function: () -> Unit) {
        var lastFailure: Throwable? = null
        repeat(count) {
            try {
                return function()
            } catch (e: Exception) {
                lastFailure = e
                Thread.sleep(delay)
            }
        }
        throw lastFailure!!
    }

    internal fun extractDownload(path: String): File {
        while (true) {
            var file: File? = null
            retry(5) {
                try {
                    downloadArchive(path)
                    file = archive.extract()
                } catch (e: IOException) {
                    LOG.error(e.message, e)
                    archive.delete()
                    downloadArchive(path)
                    Thread.sleep(1000)
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

    internal fun downloadArchive(path: String) {
        retry(5) {
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
}

private fun maxUbuntu(max: String, proposed: String): String {
    return if(proposed.contains("ubuntu") && proposed.substring(6).toInt() > max.substring(6).toInt()) {
        max
    } else {
        proposed
    }
}

internal class MongoDistribution36(version: Version) : MongoDistribution(version) {
    override fun linux() = maxUbuntu("ubuntu1604", super.linux())
    override fun linuxDownload(version: Version) = "https://fastdl.mongodb.org/linux/mongodb-linux-x86_64-${linux()}-$version.tgz"
    override fun macDownload(version: Version) = "https://fastdl.mongodb.org/osx/mongodb-osx-ssl-x86_64-$version.tgz"
    override fun windowsDownload(version: Version) = "https://fastdl.mongodb.org/win32/mongodb-win32-x86_64-2008plus-ssl-$version.zip"
}

internal class MongoDistribution40(version: Version) : MongoDistribution(version) {
    override fun linux() = maxUbuntu("ubuntu1804", super.linux())
    override fun macDownload(version: Version) = "https://fastdl.mongodb.org/osx/mongodb-osx-ssl-x86_64-$version.tgz"
    override fun windowsDownload(version: Version) = "https://fastdl.mongodb.org/win32/mongodb-win32-x86_64-2008plus-ssl-$version.zip"
}

internal class MongoDistribution42(version: Version) : MongoDistribution(version) {
    override fun linux() = maxUbuntu("ubuntu1804", super.linux())
    override fun windowsDownload(version: Version) = "https://fastdl.mongodb.org/win32/mongodb-win32-x86_64-2012plus-$version.zip"
}

internal class MongoDistribution44(version: Version) : MongoDistribution(version) {
    override fun windowsDownload(version: Version) = "https://fastdl.mongodb.org/windows/mongodb-windows-x86_64-$version.zip"
}
