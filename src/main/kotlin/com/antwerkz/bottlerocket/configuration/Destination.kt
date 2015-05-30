package com.antwerkz.bottlerocket.configuration

enum class Destination(public val name: String) {
    STANDARD_OUT: Destination("standard out")
    FILE: Destination("file")
    SYSLOG: Destination("syslog")

    fun toConfigFormat(): String {
        return name;
    }
}