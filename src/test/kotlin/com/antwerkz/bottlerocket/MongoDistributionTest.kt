package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.clusters.SingleNode
import com.github.zafarkhaja.semver.Version
import org.apache.hc.client5.http.HttpResponseException
import org.testng.Assert
import org.testng.Assert.fail
import org.testng.SkipException
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class MongoDistributionTest : BaseTest() {
    @Test
    fun addUsers() {
        SingleNode(clusterRoot = File("target/rocket/addUsersTest")).use { node ->
            node.start()
            node.addUser("test", "rocket-user", "i'm a dummy", listOf())

            try {
                node.addUser("test", "rocket-user", "i'm a dummy", listOf())
            } catch (e: Exception) {
                fail("Adding a duplicate user should not have failed.", e)
            }
        }
    }

    @Test(dataProvider = "version-matrix")
    fun downloads(version: Version, os: String) {
        val distribution = MongoDistribution.of(version)

        val url = when (os) {
            "osx" -> distribution.macDownload(version)
            "windows" -> distribution.windowsDownload(version)
            "linux" -> distribution.linuxDownload(version)
            else -> TODO()
        }

        try {
            validate(distribution, url)
        } catch (e: HttpResponseException) {
            if (version.lessThan(Version.valueOf("6.0.0"))) {
                val distro = LinuxDistribution.parse(File("/etc/os-release"))
                if (distro.name().equals("ubuntu", true)) {
                    throw SkipException("$version not supported on Ubuntu < 6.0.0")
                }

                fail("Received error code ${e.statusCode} for url $url")
            }
        }
    }

    @Test(dataProvider = "linuxDownloads", enabled = false)
    fun linux(version: Version, linuxDistribution: LinuxDistribution) {
        if(linuxDistribution.name().equals("ubuntu", true) && version.lessThan(Version.valueOf("6.0.0"))) {
            throw SkipException("$version not supported on ")
        }

        val distribution = MongoDistribution.of(version)
        distribution.linux = linuxDistribution

        validate(distribution, distribution.linuxDownload(version))
    }

    @DataProvider(name = "linuxDownloads", parallel = true)
    fun linuxDownloads(): Array<Array<Any>> {
        val downloads = Versions.list()
            .flatMap { version: Version ->
                File("src/test/resources/releases")
                    .listFiles { _, name -> name.endsWith("release") }!!
                    .map { arrayOf(version, LinuxDistribution.parse(it)) }
            }
            .toTypedArray()
        return downloads
    }

    @DataProvider(name = "version-matrix")
    private fun matrix(): Array<Array<Any>> {
        val matrix = Versions.list()
            .flatMap { version: Version ->
                listOf("osx", "windows", "linux")
                    .map { os -> arrayOf<Any>(version, os) }
            }
            .toTypedArray()

        return matrix
    }

    private fun validate(distribution: MongoDistribution, url: String) {
        distribution.downloadArchive(url)

        Assert.assertTrue(distribution.archive.exists(), "Failed to download $url")
        Assert.assertTrue(distribution.archive.length() > 0, "Failed to download $url")
        val baseDir = distribution.extractDownload(url)
        Assert.assertTrue(baseDir.exists(), "Failed to extract $url")
        Assert.assertTrue(baseDir.listFiles()?.isNotEmpty() ?: false, "Failed to extract $url")
        baseDir.deleteRecursively()
    }
}