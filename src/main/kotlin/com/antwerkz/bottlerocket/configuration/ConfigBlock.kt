package com.antwerkz.bottlerocket.configuration

import com.github.zafarkhaja.semver.Version
import com.github.zafarkhaja.semver.expr.CompositeExpression.Helper.gte
import com.github.zafarkhaja.semver.expr.CompositeExpression.Helper.lt
import kotlin.reflect.KMemberProperty
import kotlin.reflect.jvm.internal.KClassImpl
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaSetter
import kotlin.reflect.jvm.kotlin

public interface ConfigBlock {
    fun initConfigBlock<T : ConfigBlock> (configBlock: T, init: T.() -> Unit): T {
        configBlock.init()
        return configBlock
    }

    fun nodeName(): String {
        return this.javaClass.getSimpleName().toCamelCase()
    }

    open
    fun toYaml(mode: ConfigMode = ConfigMode.MONGOD, includeAll: Boolean = false): String {
        return toMap(mode, includeAll).toYaml();
    }

    fun toProperties(mode: ConfigMode = ConfigMode.MONGOD, includeAll: Boolean = false): Map<String, String> {
        return toMap(mode, includeAll).flatten();
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

    protected fun isSupportedMode(configMode: ConfigMode, property: KMemberProperty<ConfigBlock, *>): Boolean {
        try {
            val mode = getAnnotation<Mode>(property)
            val supportedMode = mode == null || mode.value == configMode || configMode == ConfigMode.ALL

            return supportedMode;
        } catch(e: Exception) {
            println("property = ${property}")
            throw e
        }
    }

    private inline fun <reified T : Annotation> getAnnotation(property: KMemberProperty<ConfigBlock, *>): T? {
        return property.javaField?.getAnnotation(javaClass())
    }

    fun toMap(mode: ConfigMode = ConfigMode.MONGOD, includeAll: Boolean = false): Map<String, Any> {
        val map = linkedMapOf<String, Any>()
        javaClass.kotlin.properties.forEach {
            if ( isSupportedMode(mode, it)) {
                val fieldValue = it.get(this)
                if (fieldValue is ConfigBlock) {
                    val fieldMap = fieldValue.toMap(mode, includeAll)
                    if (!fieldMap.isEmpty()) {
                        map.putAll(fieldMap)
                    }
                } else if ( includeAll || fieldValue != null) {
                    map.put(it.name, fieldValue.toString())
                }
            }
        }

        return linkedMapOf(Pair(nodeName(), map))
    }
}

fun Map<*, *>.flatten(prefix: String = ""): Map<String, String> {
    val map = linkedMapOf<String, String>()

    entrySet().forEach {
        if (it.value is Map<*, *>) {
            var subPrefix = if(prefix != "") prefix + "." + it.key else it.key.toString()
            map.putAll(((it.value as Map<*, *>).flatten(subPrefix)));
        } else {
            var subPrefix = if(prefix != "") prefix + "." else prefix
            map.put("${subPrefix}${it.key}", it.value.toString())
        }
    }
    return map
}

fun Map<*, *>.toYaml(indent: String = ""): String {
    val builder = StringBuilder()

    entrySet().forEach {
        if (it.value is Map<*, *>) {
            val yaml = (it.value as Map<*, *>).toYaml(indent + "  ")
            if (yaml != "") {
                builder.append("${indent}${it.key}:\n${yaml}");
            }
        } else {
            builder.append(indent)
                  .append(it.key)
                  .append(": ")
                  .append(it.value)
                  .append("\n")
        }
    }
    return builder.toString()

}

fun String.toCamelCase(): String {
    return if (length() > 1) charAt(0).toLowerCase() + substring(1) else toLowerCase()
}

