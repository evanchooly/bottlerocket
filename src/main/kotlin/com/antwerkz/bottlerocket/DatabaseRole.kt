package com.antwerkz.bottlerocket

import org.bson.Document

class DatabaseRole(val role: String, val database: String? = null) {
    fun toDB(): Any {
        if (database != null) {
            return Document("role", role).append("db", database)
        }

        return role
    }
}
