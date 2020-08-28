package com.antwerkz.bottlerocket.configuration

import com.github.zafarkhaja.semver.Version
import org.apache.http.client.fluent.Request
import org.jsoup.Jsoup
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File
import java.lang.reflect.Field
import java.net.URI
import java.util.TreeMap
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.kotlinProperty

class ConfigurationDocsTest {
    @ExperimentalStdlibApi
    @Test(dataProvider = "urls")
    fun checkDocs(version: Version) {
        val elements = loadLinks(version.docsUrl(), version)
        val properties = propertyMap(Configuration::class.java)
        val missing = check(version, properties, elements, version.docsUrl())
        Assert.assertTrue(elements.isEmpty(), "found ${elements.size} extra items in ${version.docsUrl()}: \n${
            elements.joinToString
            ("\n")
        }")
        Assert.assertTrue(missing.isEmpty(), "found ${missing.size} extra items in ${version.docsUrl()}: \n${missing.joinToString("\n")}")
    }

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

    @ExperimentalStdlibApi
    fun propertyMap(type: Class<*>): Map<String, Field> {
        val map = TreeMap<String, Field>()
        type.declaredFields.forEach { field ->
            if (ConfigBlock::class.java.isAssignableFrom(field.type)) {
                val fieldMap = propertyMap(field.type)
                fieldMap.forEach { (key, value) ->
                    map["${field.name}.$key"] = value
                }
            } else {
                map[field.name] = field
            }
        }

        return map
    }

    private fun check(version: Version, map: Map<String, Field>, elements: MutableList<String>, url: String): MutableList<String> {
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

    private fun wasAdded(field: Field, version: Version): Boolean {
        return field.kotlinProperty?.findAnnotation<Added>()?.run {
            Version.valueOf(value).greaterThanOrEqualTo(version)
        } ?: false
    }

    private fun wasRemoved(field: Field, version: Version): Boolean {
        return field.kotlinProperty?.findAnnotation<Removed>()?.run {
            Version.valueOf(value).lessThanOrEqualTo(version)
        } ?: false
    }

    @DataProvider(name = "urls")
    fun urls(): Array<Array<Any>> {
        return arrayOf(
            arrayOf(Version.forIntegers(4, 4)),
            arrayOf(Version.forIntegers(4, 2)),
            arrayOf(Version.forIntegers(4, 0)),
            arrayOf(Version.forIntegers(3, 6))
        )
    }

    private fun Version.docsUrl() = "http://docs.mongodb.org/v$majorVersion.$minorVersion/reference/configuration-options/"
}