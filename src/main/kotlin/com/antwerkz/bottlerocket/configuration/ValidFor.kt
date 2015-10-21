package com.antwerkz.bottlerocket.configuration

//Retention(RUNTIME)
//annotation class ValidFor(val introduced: String = "1.0.0", val removed: String = "100.0.0")

@Retention
annotation class Mode(val value: ConfigMode)

enum class ConfigMode {
    ALL,
    MONGOD,
    MONGOS
}