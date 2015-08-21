package com.antwerkz.bottlerocket

import org.bson.Document

public class DatabaseRole(public val role: String, public val database: String? = null) {
    fun toDB(): Any {
        if(database != null) {
            return Document("role", role)
                  .append("db", database)
        }

        return role
    }
}