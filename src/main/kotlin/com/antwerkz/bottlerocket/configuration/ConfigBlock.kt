package com.antwerkz.bottlerocket.configuration

import com.github.zafarkhaja.semver.Version
import kotlin.reflect.KMemberProperty
import kotlin.reflect.KMutableMemberProperty
import kotlin.reflect.jvm.internal.KClassImpl
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaSetter

public interface ConfigBlock {
    companion object {
        var OMIT_DEFAULTED = true
    }

    fun initConfigBlock<T : ConfigBlock> (configBlock: T, init: T.() -> Unit): T {
        configBlock.init()
        return configBlock
    }

    fun nodeName(): String? {
        val c = KClassImpl(this.javaClass)
        return c.simpleName?.substring(0, 1)?.toLowerCase() + c.simpleName?.substring(1)

    }

    open fun toYaml(version: Version = Version.valueOf("3.0.0"), mode: ConfigMode = ConfigMode.MONGOD): String {
        val list = arrayListOf<String>()
        val c = KClassImpl(this.javaClass)

        val comparison = c.jClass.newInstance()
        c.properties.forEach {
            if ( isValidForContext(version, mode, it)) {
                val fieldValue = it.get(this)
                if ( fieldValue != null ) {
                    var value: String
                    if (fieldValue is ConfigBlock) {
                        val yaml = fieldValue.toYaml(version, mode)
                        if ( yaml != "" ) {
                            value = yaml.split('\n').map { it -> "  ${it}" }.join("\n").trim()
                            list.add("  ${value}")
                        }
                    } else {
                        if ( !OMIT_DEFAULTED || fieldValue != it.get(comparison)) {
                            list.add("  ${it.name}: ${fieldValue}")
                        }
                    }
                }
            }
        }

        return if (list.isEmpty()) "" else "${nodeName()}:\n${list.join("\n")}" ;
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
        val b1 = mode == null || mode.value == configMode
        return b && b1;
        } catch( e: Exception ) {
            println("property = ${property}")
            throw e
        }
    }
}

