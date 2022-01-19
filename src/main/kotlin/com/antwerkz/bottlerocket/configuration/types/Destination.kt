package com.antwerkz.bottlerocket.configuration.types

enum class Destination(val fileValue: String) {
    STANDARD_OUT("standard out"),
    FILE("file"),
    SYSLOG("syslog");

    override fun toString(): String {
        return fileValue
    }
}