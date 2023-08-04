package com.antwerkz.bottlerocket

import com.github.zafarkhaja.semver.Version
import java.net.URL
import org.jsoup.Jsoup
import org.testng.Assert.assertFalse
import org.testng.Assert.assertTrue
import org.testng.annotations.Test

@Test
class VersionsTest {
    fun latest() {
        val doc = Jsoup.parse(URL("https://www.mongodb.com/try/download/community"), 10000)
        val found =
            doc.getElementById("download-version")!!
                .siblingElements()
                .flatMap { it.getElementsByTag("div") }
                .flatMap { it.getElementsByTag("ul") }
                .flatMap { it.getElementsByTag("li") }
                .map { it.text().substringBefore(" ") }
                .map { Version.valueOf(it) }
                .filter { it.preReleaseVersion == null || it.preReleaseVersion == "" }
                .filter { it.greaterThan(Version.valueOf("4.0.0")) }

        assertFalse(found.isEmpty(), "Should find versions")
        val versions = found.filter { it !in Versions.list() }

        assertTrue(versions.isEmpty(), "Some versions are out of date: ${versions}")
    }
}
