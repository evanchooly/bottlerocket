package com.antwerkz.bottlerocket.configuration

import org.apache.http.client.fluent.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.slf4j.LoggerFactory
import org.testng.Assert
import org.testng.annotations.BeforeTest
import org.testng.annotations.Test
import java.io.File
import java.net.URI

public class ConfigurationDocsTest {
    companion object {
        private val LOG = LoggerFactory.getLogger(javaClass<ConfigurationDocsTest>())
    }

    val htmlCache = hashMapOf<File, Document>()

    private var elements: MutableList<String> = arrayListOf()

    @BeforeTest
    fun checkLinks() {
        val url = URI("http://docs.mongodb.org/manual/reference/configuration-options/")
        val file = File("build", "configuration-options.html")

        if (!file.exists()) {
            Request.Get(url)
                  .execute()
                  .saveContent(file)
        }
        val doc = Jsoup.parse(file, "UTF-8")
        elements = doc.select("a[class=headerlink]")
              .filter({ it.attr("href").contains('.') })
              .map({ it.attr("href") })
              .toArrayList()
    }

    @Test
    fun checkDocs() {
        check(Configuration().toProperties(mode = ConfigMode.ALL, includeAll = true))
        Assert.assertTrue(elements.isEmpty(), "elements should be empty now but has ${elements.size()} items left: \n${elements}")
    }

    private fun check(map: Map<String, Any>) {
        map.keySet().forEach {
            Assert.assertTrue(elements.remove("#${it}"), "Should find #${it} in the docs.");
        }
    }

}