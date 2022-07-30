package com.antwerkz.bottlerocket

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.zafarkhaja.semver.Version
import org.jsoup.Jsoup
import org.testng.Assert.assertTrue
import org.testng.annotations.Test
import java.net.URL

@Test
class VersionsTest {
    @Suppress("UNCHECKED_CAST")
    fun latest() {
        val doc = Jsoup.parse(URL("https://www.mongodb.com/try/download/community"), 10000)
        val mdbInput = doc.getElementsByTag("mdb-input")
            .first { it.attr("label").equals("Version") }
        val mapper = ObjectMapper().createParser(mdbInput.attr("options"))
        val versions = mapper.readValueAs(List::class.java)
            .map { Version.valueOf((it as Map<String, String>)["value"]) }
            .filter { it.greaterThan(Version.valueOf("4.0.0"))}
            .filter { it.preReleaseVersion == "" }
            .filter { it !in Versions.list() }

        assertTrue(versions.isEmpty(), "Some versions are out of date: ${versions}")
    }
}