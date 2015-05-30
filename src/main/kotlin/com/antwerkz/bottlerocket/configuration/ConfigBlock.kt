package com.antwerkz.bottlerocket.configuration

import kotlin.reflect.KClass
import kotlin.reflect.jvm.internal.KClassImpl

public trait ConfigBlock {
    companion object {
        var OMIT_DEFAULTED = true
    }

    fun nodeName(): String? {
        val c = KClassImpl(this.javaClass)
        return c.simpleName?.substring(0, 1)?.toLowerCase() + c.simpleName?.substring(1)

    }

    fun toYaml(): String {
        val list = arrayListOf<String>()
        val c = KClassImpl(this.javaClass)

        val comparison = c.jClass.newInstance()
        c.properties.forEach {
            val fieldValue = it.get(this)
            if ( !OMIT_DEFAULTED || fieldValue != null ) {
                var value: String
                if (fieldValue is ConfigBlock) {
                    val yaml = fieldValue.toYaml()
                    if ( yaml != "" ) {
                        value = yaml.split('\n').map { it -> "  ${it}" }.join("\n").trim()
                        list.add("  ${value}")
                    }
                } else {
                    if ( !OMIT_DEFAULTED || fieldValue != it.get(comparison)) {
                        list.add("  ${it.name}: ${fieldValue ?: ""}")
                    }
                }
            }
        }

        return if (list.isEmpty()) "" else "${nodeName()}:\n${list.join("\n")}" ;
    }
}

