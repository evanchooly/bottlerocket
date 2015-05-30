package com.antwerkz.bottlerocket.configuration

enum class Destination(public val fileValue: String) {
    STANDARD_OUT("standard out"),
    FILE("file"),
    SYSLOG("syslog");

    override fun toString(): String {
        return fileValue;
    }
}