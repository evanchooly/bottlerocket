package com.antwerkz.bottlerocket.configuration

import java.lang.annotation.RetentionPolicy

//Retention(RUNTIME)
//annotation class ValidFor(val introduced: String = "1.0.0", val removed: String = "100.0.0")

@Retention
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
annotation class Mode(val value: ConfigMode)

enum class ConfigMode {
    ALL,
    MONGOD,
    MONGOS
}