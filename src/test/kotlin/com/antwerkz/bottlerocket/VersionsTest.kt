package com.antwerkz.bottlerocket

import com.github.zafarkhaja.semver.Version
import org.jsoup.Jsoup
import org.testng.Assert.assertTrue
import org.testng.annotations.Test
import java.net.URL

@Test
class VersionsTest {
    fun latest() {
        val doc = Jsoup.parse(URL("https://www.mongodb.com/try/download/community"), 10000)
        val versions = doc.getElementById("download-version")!!
            .siblingElements()
            .flatMap { it.getElementsByTag("div") }
            .flatMap { it.getElementsByTag("ul") }
            .flatMap { it.getElementsByTag("li") }
            .map { it.text().substringBefore(" ") }
            .map { Version.valueOf(it) }
            .filter { it.greaterThan(Version.valueOf("4.0.0")) }
            .filter { it !in Versions.list() }

        assertTrue(versions.isEmpty(), "Some versions are out of date: ${versions}")
    }
}