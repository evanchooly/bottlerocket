package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.clusters.SingleNode
import com.github.zafarkhaja.semver.Version
import org.testng.Assert
import org.testng.Assert.fail
import org.testng.annotations.Test
import java.io.File

class MongoMangerTest {
    @Test(enabled = false)  // disabled by default to avoid churn. let OS failures get picked up on travis first
    fun downloads() {
        for (os in listOf("windows", "linux", "osx")) {
            for (version in listOf(Version.valueOf("4.2.8"), Version.valueOf("4.0.19"), Version.valueOf("3.6.18"))) {
                val manager = MongoManager.of(version)
                manager.downloadPath = File("target")
                val url = when (os) {
                    "linux" -> manager.linuxDownload(version)
                    "osx" -> manager.macDownload(version)
                    "windows" -> manager.windowsDownload(version)
                    else -> TODO()
                }
                val archive = try {
                    manager.downloadArchive(url)
                } catch (e: Exception) {
                    fail("Failed to download $url")
                    throw e
                }
                Assert.assertTrue(archive.exists(), "Failed to download $url")
                Assert.assertTrue(archive.length() > 0, "Failed to download $url")
                val baseDir = manager.extractDownload(url)
                Assert.assertTrue(baseDir.exists(), "Failed to extract $url")
                Assert.assertTrue(baseDir.listFiles()?.isNotEmpty() ?: false, "Failed to extract $url")
                archive.delete()
                baseDir.deleteRecursively()
            }
        }
    }

    @Test
    fun addUsers() {
        val node = SingleNode
            .builder()
            .build()

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
}