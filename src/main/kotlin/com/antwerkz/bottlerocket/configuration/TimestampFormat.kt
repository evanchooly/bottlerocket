package com.antwerkz.bottlerocket.configuration

enum class TimestampFormat(val name: String) {
    CTIME("ctime"),
    ISO8601_UTC("iso8601-utc"),
    ISO8601_LOCAL("iso8601-local");

    fun toConfigFormat(): String {
        return name;
    }

}