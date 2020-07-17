package com.antwerkz.bottlerocket.configuration.archived.mongo30

import com.antwerkz.bottlerocket.MongoManager
import com.antwerkz.bottlerocket.runCommand
import com.github.zafarkhaja.semver.Version
import com.mongodb.client.MongoClient
import org.bson.Document

abstract class MongoManager30(version: Version): MongoManager(version) {
    override fun getReplicaSetConfig(client: MongoClient): Document {
        return client.runCommand(Document("replSetGetConfig", 1))["config"] as Document
    }
}
