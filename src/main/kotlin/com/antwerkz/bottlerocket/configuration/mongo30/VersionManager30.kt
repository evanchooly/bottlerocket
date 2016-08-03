package com.antwerkz.bottlerocket.configuration.mongo30

import com.antwerkz.bottlerocket.configuration.mongo26.VersionManager26
import com.antwerkz.bottlerocket.runCommand
import com.github.zafarkhaja.semver.Version
import com.mongodb.MongoClient
import org.bson.Document
import com.antwerkz.bottlerocket.configuration.Configuration as BaseConfiguration

open class VersionManager30(version: Version) : VersionManager26(version) {
    override fun getReplicaSetConfig(client: MongoClient): Document {
        return client.runCommand(Document("replSetGetConfig", 1))["config"] as Document
    }
}