package com.antwerkz.bottlerocket.configuration

import com.antwerkz.bottlerocket.configuration.mongo30.Configuration as Config30
import com.antwerkz.bottlerocket.configuration.mongo26.Configuration as Config26
import com.antwerkz.bottlerocket.configuration.mongo24.Configuration as Config24
import com.antwerkz.bottlerocket.configuration.mongo22.Configuration as Config22
import com.antwerkz.bottlerocket.configuration.mongo30.blocks.Component
import com.antwerkz.bottlerocket.configuration.mongo30.blocks.SystemLog
import com.github.zafarkhaja.semver.Version
import org.apache.http.client.fluent.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.slf4j.LoggerFactory
import org.testng.Assert
import org.testng.annotations.BeforeTest
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File
import java.net.URI
import java.util.*

public class ConfigurationDocsTest {
    private var elements: MutableList<String> = arrayListOf()

    fun loadLinks(url: String, version: String) {
        val uri = URI(url)
        val file = File("build", "${version}-configuration-options.html")

        if (!file.exists()) {
            Request.Get(uri)
                  .execute()
                  .saveContent(file)
        }
        val doc = Jsoup.parse(file, "UTF-8")
        elements = when(version) {
            "3.0.0", "2.6.0" -> {
                doc.select("a[class=headerlink]")
                      .filter({ it.attr("href").contains('.') })
                      .map({ it.attr("href") })
                      .toArrayList()

            }
            "2.4.0", "2.2.0" -> {
                var inSettings = false
                doc.select("a[class=headerlink]")
                      .filter({
                          val include = inSettings
                                && !it.attr("href").endsWith("-options")
                                && !it.attr("href").equals("#setParameter")
                                && !it.attr("href").equals("#master-slave-replication")
                                && !it.attr("href").startsWith("#cmdoption--")
                          inSettings = inSettings || it.attr("href").contains("settings")
                          include
                      })
                      .map({ it.attr("href") })
                      .toArrayList()

            }
            else -> throw IllegalArgumentException("Unknown version: ${version}");
        }
    }

    @Test(dataProvider = "urls")
    fun checkDocs(version: String, url: String, configuration: ConfigBlock) {
        loadLinks(url, version)
        check(configuration.toProperties(mode = ConfigMode.ALL, includeAll = true))
        Assert.assertTrue(elements.isEmpty(), "elements should be empty now but has ${elements.size()} items left: \n${elements}")
    }

    private fun check(map: Map<String, String>) {
        map.keySet().forEach {
            Assert.assertTrue(elements.remove("#${it}"), "Found ${it} in the configuration file but not in the docs.");
        }
    }

    @DataProvider(name = "urls")
    fun urls(): Array<Array<Any>> {
        return arrayOf(
              arrayOf("3.0.0", "http://docs.mongodb.org/v3.0/reference/configuration-options/", Config30()),
              arrayOf("2.6.0", "http://docs.mongodb.org/v2.6/reference/configuration-options/", Config26()),
              arrayOf("2.4.0", "http://docs.mongodb.org/v2.4/reference/configuration-options/", Config24()),
              arrayOf("2.2.0", "http://docs.mongodb.org/v2.2/reference/configuration-options/", Config22())
        )
    }
}