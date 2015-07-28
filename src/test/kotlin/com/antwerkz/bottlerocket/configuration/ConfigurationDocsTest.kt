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
        check(Configuration().toProperties(omitDefaults = false, mode = ConfigMode.ALL))
        Assert.assertTrue(elements.isEmpty(), "elements should be empty now but has ${elements.size()} items left: \n${elements}")
    }

    private fun check(map: Map<String, Any>) {
        map.keySet().forEach {
            Assert.assertTrue(elements.remove("#${it}"), "Should find #${it} in the docs.");
        }
        /*
                map.keySet().forEach { property: KMemberProperty<T, *> ->
                    try {
                        val field = property.javaField
                        if (field != null) {
                            val javaClazz = field.getType()
                            if (ConfigBlock::class.java.isAssignableFrom(javaClazz)) {
                                check(javaClazz.kotlin as KClass<T>)
                            } else {
                                var name = field.getName()?.toCamelCase();
                                elements.remove("#${clazz.simpleName?.toCamelCase()}.${name?.toCamelCase()}");
                            }
                        } else {
                            LOG.debug("${property}'s javaField is null")
                        }
                    } catch(e: NullPointerException) {
                        println("property = ${property}")
                        throw e;
                    }
                }
        */
    }

    private fun validateUrl(name: String?, value: String?) {
        if (value == null || value.isBlank()) {
            Assert.fail("URL for ${name} can not be blank.")
        }

        val url = URI(value)
        val file = File("build", url.getPath().replace('/', '_'))

        if (!file.exists()) {
            Request.Get(value)
                  .execute()
                  .saveContent(file)
        }
        if (!htmlCache.containsKey(file)) {
            htmlCache.put(file, Jsoup.parse(file, "UTF-8"))
        }

        Assert.assertTrue(htmlCache.get(file).select("a[href=#${url.getFragment()}]").isNotEmpty(),
              "Should find a match for ${"a[href=#${url.getFragment()}]"}")
    }
}