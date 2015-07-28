package com.antwerkz.bottlerocket.configuration

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy.RUNTIME

Retention(RUNTIME)
annotation class Since(val version: String)

Retention(RUNTIME)
annotation class Mode(val value: ConfigMode)

enum class ConfigMode {
    ALL,
    MONGOD,
    MONGOS
}