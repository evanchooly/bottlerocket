package com.antwerkz.bottlerocket.configuration

enum class TimestampFormat(val name: String) {
    CTIME : TimestampFormat("ctime")
    ISO8601_UTC: TimestampFormat("iso8601-utc")
    ISO8601_LOCAL: TimestampFormat("iso8601-local")

    fun toConfigFormat(): String {
        return name;
    }

}