package com.antwerkz.bottlerocket.configuration

enum class Destination(public val fileValue: String) {
    STANDARD_OUT: Destination("standard out")
    FILE: Destination("file")
    SYSLOG: Destination("syslog")

    override fun toString(): String {
        return fileValue;
    }
}