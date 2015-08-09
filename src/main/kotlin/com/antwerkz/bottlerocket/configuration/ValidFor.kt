package com.antwerkz.bottlerocket.configuration

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy.RUNTIME

//Retention(RUNTIME)
//annotation class ValidFor(val introduced: String = "1.0.0", val removed: String = "100.0.0")

Retention(RUNTIME)
annotation class Mode(val value: ConfigMode)

enum class ConfigMode {
    ALL,
    MONGOD,
    MONGOS
}