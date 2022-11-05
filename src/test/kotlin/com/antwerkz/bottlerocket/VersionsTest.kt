package com.antwerkz.bottlerocket

import com.github.zafarkhaja.semver.Version
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.testng.Assert.assertTrue
import org.testng.annotations.Test
import java.net.URL

@Test
class VersionsTest {
    @Suppress("UNCHECKED_CAST")
    fun latest() {
        val doc = Jsoup.parse(URL("https://www.mongodb.com/try/download/community"), 10000)
        val versions = doc.getElementById("download-version")!!.siblingElements()
            .filter { element -> element.tagName() != "style" }
            .filter { element -> element.tagName() == "div" && element.attr("role") == "dropdown" }
            .flatMap { it.children() }
            .filter { element -> element.tagName() != "style" }
            .flatMap { it.children() }
            .filter { element -> element.tagName() != "style" }
            .map { it.text().substringBefore(" ") }
            .map { Version.valueOf(it) }
            .filter { it.greaterThan(Version.valueOf("4.0.0"))}
            .filter { it !in Versions.list() }

        assertTrue(versions.isEmpty(), "Some versions are out of date: ${versions}")
    }
}