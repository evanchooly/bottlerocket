package com.antwerkz.bottlerocket.configuration

import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.javaSetter
import kotlin.reflect.memberProperties
import kotlin.reflect.primaryConstructor

interface ConfigBlock {
    fun <T : ConfigBlock> initConfigBlock(configBlock: T, init: T.() -> Unit): T {
        configBlock.init()
        return configBlock
    }

    fun nodeName(): String {
        return this.javaClass.simpleName.toCamelCase()
    }

    open
    fun toYaml(mode: ConfigMode = ConfigMode.MONGOD, includeAll: Boolean = false): String {
        return toMap(mode, includeAll).toYaml();
    }

    fun toProperties(mode: ConfigMode = ConfigMode.MONGOD, includeAll: Boolean = false): Map<String, String> {
        return toMap(mode, includeAll).flatten();
    }

    fun merge(update: ConfigBlock) {
        val c = this.javaClass.kotlin

        val comparison: ConfigBlock = c.primaryConstructor?.callBy(mapOf())!!
        val target = this
        c.memberProperties.forEach { p: KProperty1<ConfigBlock, *> ->
            val fieldValue = p.get(update)
            if ( fieldValue != null ) {
                if (fieldValue is ConfigBlock) {
                    (p.get(target) as ConfigBlock).merge(fieldValue)
                } else {
                    if ( fieldValue != p.get(comparison)) {
                        (p as KMutableProperty<*>).javaSetter?.invoke(target, fieldValue)
                    }
                }
            }
        }

    }

    fun isSupportedMode(configMode: ConfigMode, property: KProperty1<ConfigBlock, *>): Boolean {
        val mode = getAnnotation<Mode>(property) as Mode?
        return mode == null || mode.value == configMode || configMode == ConfigMode.ALL;
    }

    private inline fun <reified T : Annotation> getAnnotation(property: KProperty1<ConfigBlock, *>): Annotation? {
        return property.annotations.firstOrNull { it is T }
    }

    fun toMap(mode: ConfigMode = ConfigMode.MONGOD, includeAll: Boolean = false): Map<String, Any> {
        val map = linkedMapOf<String, Any>()
        javaClass.kotlin.memberProperties.forEach {
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

    entries.forEach {
        if (it.value is Map<*, *>) {
            var subPrefix = if(prefix != "") prefix + "." + it.key else it.key.toString()
            map.putAll(((it.value as Map<*, *>).flatten(subPrefix)));
        } else {
            var subPrefix = if(prefix != "") prefix + "." else prefix
            map.put("$subPrefix${it.key}", it.value.toString())
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
                builder.append("$indent${it.key}:\n$yaml");
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