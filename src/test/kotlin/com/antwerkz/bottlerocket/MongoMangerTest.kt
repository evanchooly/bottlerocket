package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.clusters.SingleNode
import com.github.zafarkhaja.semver.Version
import org.testng.Assert
import org.testng.Assert.fail
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class MongoMangerTest: BaseTest() {
    @Test
    fun addUsers() {
        val node = SingleNode(File("target/rocket/addUsersTest"), allocator = portAllocator)

        node.start()

        node.addUser("test", "rocket-user", "i'm a dummy", listOf())

        try {
            node.addUser("test", "rocket-user", "i'm a dummy", listOf())
        } catch (e: Exception) {
            fail("Adding a duplicate user should not have failed.", e)
        } finally {
            node.shutdown()
        }
    }

    // disabled by default to avoid churn. let OS failures get picked up on travis first
    @Test(dataProvider = "version-matrix", enabled = false)
    fun downloads(version: Version, os: String) {
        val distribution = MongoDistribution.of(version)

        distribution.downloadPath = File("target")
        val url = when (os) {
            "osx" -> distribution.macDownload(version)
            "windows" -> distribution.windowsDownload(version)
            else -> TODO()
        }

        validate(distribution, url)
    }

    @Test(dataProvider = "linuxDownloads", enabled = false)
    fun linux(version: Version, linuxDistribution: LinuxDistribution) {
        val distribution = MongoDistribution.of(version)
        distribution.linux = linuxDistribution
        distribution.downloadPath = File("target")

        validate(distribution, distribution.linuxDownload(version))
    }

    @DataProvider(name = "linuxDownloads")
    fun linuxDownloads(): Array<Array<Any>> {
        val downloads = Versions.list()
            .flatMap { version: Version ->
                File("src/test/resources/releases")
                    .listFiles { dir, name -> name.endsWith("release") }!!
                    .map { arrayOf(version, LinuxDistribution.parse(it)) }
            }
            .toTypedArray()
        return downloads
    }

    @DataProvider(name = "version-matrix")
    private fun matrix(): Array<Array<Any>> {
        val matrix = Versions.list()
            .flatMap { version: Version ->
                listOf("osx", "windows")
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