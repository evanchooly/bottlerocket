package com.antwerkz.bottlerocket.configuration

import com.github.zafarkhaja.semver.Version
import org.apache.http.client.fluent.Request
import org.jsoup.Jsoup
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File
import java.net.URI
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation

class ConfigurationDocsTest {
    private fun loadLinks(url: String, version: Version): MutableList<String> {
        val uri = URI(url)
        val file = File("target", "$version-configuration-options.html")

        if (!file.exists()) {
            Request.Get(uri)
                .execute()
                .saveContent(file)
        }
        val doc = Jsoup.parse(file, "UTF-8")
        return doc.select("a[class=headerlink]")
            .filter { it.attr("href").contains('.') }
            .map { it.attr("href").substring(1) }
            .sorted()
            .toMutableList()
    }

    @Test(dataProvider = "urls")
    fun checkDocs(version: Version, url: String) {
        val elements = loadLinks(url, version)
        val properties = configuration {
        }.toLookups()
        val missing = check(version, properties, elements, url)
        Assert.assertTrue(elements.isEmpty(), "found ${elements.size} extra items in $url: \n${elements.joinToString("\n")}")
        Assert.assertTrue(missing.isEmpty(), "found ${missing.size} extra items in $url: \n${missing.joinToString("\n")}")
    }

    private fun check(version: Version, map: Map<String, KProperty<*>>, elements: MutableList<String>, url: String): MutableList<String> {
        val outdated = mutableListOf<String>()
        map.forEach { (key, value) ->
            val present = elements.remove(key)
            var annotated = false
            if (!present) {
                annotated = wasRemoved(value, version) ||
                    wasAdded(value, version)
            }
            if (!present && !annotated) outdated += "Found $key in the model but not in the configuration: $url"
        }

        Assert.assertTrue(outdated.isEmpty(), "Found mismatches between docs and code: " + outdated.joinToString("\n", "\n"))
        return outdated
    }

    private fun wasAdded(value: KProperty<*>, version: Version): Boolean {
        return value.findAnnotation<Added>()?.run {
            Version.valueOf(this.value).greaterThanOrEqualTo(version)
        } ?: false
    }

    private fun wasRemoved(value: KProperty<*>, version: Version): Boolean {
        return value.findAnnotation<Removed>()?.run {
            Version.valueOf(this.value).lessThanOrEqualTo(version)
        } ?: false
    }

    @DataProvider(name = "urls")
    fun urls(): Array<Array<Any>> {
        return arrayOf(
            arrayOf<Any>(Version.forIntegers(4, 2), "http://docs.mongodb.org/v4.2/reference/configuration-options/"),
            arrayOf<Any>(Version.forIntegers(4, 0), "http://docs.mongodb.org/v4.0/reference/configuration-options/"),
            arrayOf<Any>(Version.forIntegers(3, 6), "http://docs.mongodb.org/v3.6/reference/configuration-options/")
        )
    }
}