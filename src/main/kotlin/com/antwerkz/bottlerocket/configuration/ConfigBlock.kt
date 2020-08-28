package com.antwerkz.bottlerocket.configuration

import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

interface ConfigBlock {
    fun <T : ConfigBlock> initConfigBlock(configBlock: T, init: T.() -> Unit): T {
        configBlock.init()
        return configBlock
    }

    fun nodeName(): String {
        return this.javaClass.simpleName.toCamelCase()
    }

    fun toYaml(mode: ConfigMode = ConfigMode.MONGOD, includeAll: Boolean = false): String {
        return toMap(mode, includeAll).toYaml()
    }

    fun isSupportedMode(configMode: ConfigMode, property: KProperty1<ConfigBlock, *>): Boolean {
        val mode = getAnnotation<Mode>(property)
        return mode == null || mode.value == configMode || configMode == ConfigMode.ALL
    }

    private inline fun <reified T : Annotation> getAnnotation(property: KProperty1<ConfigBlock, *>): T? {
        return property.annotations
            .filterIsInstance(T::class.java)
            .firstOrNull()
    }

    fun toMap(mode: ConfigMode = ConfigMode.MONGOD, includeAll: Boolean = false): Map<String, Any> {
        val map = linkedMapOf<String, Any>()
        javaClass.kotlin.memberProperties.forEach {
            if (isSupportedMode(mode, it)) {
                val fieldValue = it.get(this)
                if (fieldValue is ConfigBlock) {
                    val fieldMap = fieldValue.toMap(mode, includeAll)
                    if (!fieldMap.isEmpty()) {
                        map.putAll(fieldMap)
                    }
                } else if (includeAll || fieldValue != null) {
                    map[it.name] = fieldValue.toString()
                }
            }
        }

        return linkedMapOf(Pair(nodeName(), map))
    }
}

fun Map<*, *>.flatten(prefix: String = ""): Map<String, String> {
    val map = linkedMapOf<String, String>()

    entries.forEach {
        if (it.value is Map<*, *>) {
            val subPrefix = if (prefix != "") prefix + "." + it.key else it.key.toString()
            map.putAll(((it.value as Map<*, *>).flatten(subPrefix)))
        } else {
            val subPrefix = if (prefix != "") prefix + "." else prefix
            map["$subPrefix${it.key}"] = it.value.toString()
        }
    }
    return map
}

fun Map<*, *>.toYaml(indent: String = ""): String {
    val builder = StringBuilder()

    entries.forEach {
        if (it.value is Map<*, *>) {
            val yaml = (it.value as Map<*, *>).toYaml(indent + "  ")
            if (yaml != "") {
                builder.append("$indent${it.key}:\n$yaml")
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
    return if (length > 1) this[0].toLowerCase() + substring(1) else toLowerCase()
}