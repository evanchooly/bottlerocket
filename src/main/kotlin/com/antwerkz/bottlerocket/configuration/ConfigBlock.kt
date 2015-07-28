package com.antwerkz.bottlerocket.configuration

import com.github.zafarkhaja.semver.Version
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import kotlin.reflect.KMemberProperty
import kotlin.reflect.jvm.internal.KClassImpl
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaSetter

public interface ConfigBlock {
    fun initConfigBlock<T : ConfigBlock> (configBlock: T, init: T.() -> Unit): T {
        configBlock.init()
        return configBlock
    }

    fun nodeName(): String? {
        return this.javaClass.getSimpleName()?.toCamelCase()
    }

    open fun toYaml(version: Version = Version.valueOf("3.0.0"), mode: ConfigMode = ConfigMode.MONGOD, omitDefaults: Boolean = true):
          String {
        val list = arrayListOf<String>()
        val c = KClassImpl(this.javaClass)

        val comparison = c.jClass.newInstance()
        c.properties.forEach {
            if ( isValidForContext(version, mode, it)) {
                val fieldValue = it.get(this)
                if ( fieldValue != null ) {
                    var value: String
                    if (fieldValue is ConfigBlock) {
                        val yaml = fieldValue.toYaml(version, mode, omitDefaults)
                        if ( yaml != "" ) {
                            value = yaml.split('\n').map { it -> "  ${it}" }.join("\n").trim()
                            list.add("  ${value}")
                        }
                    } else if ( !omitDefaults || fieldValue != it.get(comparison)) {
                        list.add("  ${it.name}: ${fieldValue}")
                    }
                }
            }
        }

        return if (list.isEmpty()) "" else "${nodeName()}:\n${list.join("\n")}" ;
    }

    fun toProperties(version: Version = Version.valueOf("3.0.0"), mode: ConfigMode = ConfigMode.MONGOD,
                     omitDefaults: Boolean = true): Map<String, Any> {
        val map = linkedMapOf<String, Any>()
        val c = KClassImpl(this.javaClass)

        val comparison = c.jClass.newInstance()
        c.properties.forEach {
            if ( isValidForContext(version, mode, it)) {
                val fieldValue = it.get(this)
                if (fieldValue is ConfigBlock) {
                    fieldValue
                          .toProperties(version, mode, omitDefaults)
                          .forEach {
                              map.put("${nodeName()}.${it.key}", it.value)
                          }
                } else if ( !omitDefaults || fieldValue != null && fieldValue != it.get(comparison)) {
                    map.put("${nodeName()}.${it.name}", fieldValue)
                }
            }
        }

        return map;
    }

    fun merge(update: ConfigBlock) {
        val c = KClassImpl(this.javaClass)

        val comparison = c.jClass.newInstance()
        val target = this
        c.properties.forEach { p ->
            val fieldValue = p.get(update)
            if ( fieldValue != null ) {
                if (fieldValue is ConfigBlock) {
                    (p.get(target) as ConfigBlock).merge(fieldValue)
                } else {
                    if ( fieldValue != p.get(comparison)) {
                        c.mutableMemberProperty(p.name).javaSetter?.invoke(target, fieldValue)
                    }
                }
            }
        }

    }

    protected fun isValidForContext(version: Version, configMode: ConfigMode, property: KMemberProperty<ConfigBlock, *>): Boolean {
        val since = property.javaField?.getAnnotation(javaClass<Since>())
        val mode = property.javaField?.getAnnotation(javaClass<Mode>())
        try {
            val b = since == null || Version.valueOf(since.version).lessThanOrEqualTo(version)
            val b1 = mode == null || mode.value == configMode || configMode == ConfigMode.ALL
            return b && b1;
        } catch(e: Exception) {
            println("property = ${property}")
            throw e
        }
    }
}

fun String.toCamelCase(): String {
    return if (length() > 1) charAt(0).toLowerCase() + substring(1) else toLowerCase()
}

