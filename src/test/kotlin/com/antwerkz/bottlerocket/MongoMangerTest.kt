package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.clusters.SingleNode
import org.testng.Assert
import org.testng.Assert.fail
import org.testng.annotations.Test
import java.io.File

class MongoMangerTest {
    // disabled by default to avoid churn. let OS failures get picked up on travis first
    @Test(enabled = false)
    fun downloads() {
        for (os in listOf("windows", "linux", "osx")) {
            for (version in Versions.list()) {
                val manager = MongoManager.of(version)
                manager.downloadPath = File("target")
                val url = when (os) {
                    "linux" -> manager.linuxDownload(version)
                    "osx" -> manager.macDownload(version)
                    "windows" -> manager.windowsDownload(version)
                    else -> TODO()
                }

                manager.downloadArchive(url)

                Assert.assertTrue(manager.archive.exists(), "Failed to download $url")
                Assert.assertTrue(manager.archive.length() > 0, "Failed to download $url")
                val baseDir = manager.extractDownload(url)
                Assert.assertTrue(baseDir.exists(), "Failed to extract $url")
                Assert.assertTrue(baseDir.listFiles()?.isNotEmpty() ?: false, "Failed to extract $url")
                manager.archive.delete()
                baseDir.deleteRecursively()
            }
        }
    }

    @Test
    fun addUsers() {
        val node = SingleNode()

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