package com.antwerkz.bottlerocket.configuration.mongo26

import com.antwerkz.bottlerocket.BaseVersionManager
import com.antwerkz.bottlerocket.ReplicaSet
import com.antwerkz.bottlerocket.configuration.Configuration
import com.antwerkz.bottlerocket.configuration.types.Destination.FILE
import com.antwerkz.bottlerocket.executable.Mongod
import com.github.zafarkhaja.semver.Version
import org.bson.Document
import java.io.File
import java.util.ArrayList

class VersionManager26(version: Version) : BaseVersionManager(version) {
    override fun setReplicaSetName(node: Mongod, name: String) {
        val configuration = node.config as Configuration26
        configuration.replication.replSetName = name
    }

/*
    override fun initialize(replicaSet: ReplicaSet) {
        if (replicaSet.initialized) {
            return;
        }

        initiateReplicaSet(replicaSet)
        BaseVersionManager.LOG.info("replSet initiated.  waiting for primary.")
        replicaSet.waitForPrimary()
        BaseVersionManager.LOG.info("primary found.  adding other members.")
        addMembers(replicaSet)

        replicaSet.waitForPrimary()
        replicaSet.initialized = true;

        BaseVersionManager.LOG.info("replica set ${replicaSet.name} started.")
    }

    private fun addMembers(replicaSet: ReplicaSet) {
        // used to be if(null) throw
        val primary = replicaSet.getPrimary() ?: throw IllegalStateException("Replica set ${replicaSet.name} has no primary")

        val config = getReplicaSetConfig(primary)
        config.set("version", config.getInteger("version") + 1)
        val members: ArrayList<Document> = config.get("members") as ArrayList<Document>
        var id = members[0].getInteger("_id")
        replicaSet.nodes.asSequence().withIndex()
              .filter({ it.index > 0 })
              .map { Document("_id", ++id).append("host", "localhost:${it.value.port}") }
              .toCollection(members)

        val results = primary.runCommand(Document("replSetReconfig", config));

        if ( results.getDouble("ok").toInt() != 1) {
            throw RuntimeException("Failed to add members to replica set:  ${results}")
        }
    }
*/

    override fun getReplicaSetConfig(primary: Mongod) = primary.getClient().getDatabase("local").getCollection(
          "system.replset").find().limit(1).first()

    override fun initialConfig(baseDir: File, name: String, port: Int): Configuration {
        return configuration {
            net {
                this.port = port
            }
            processManagement {
                pidFilePath = File(baseDir, "${name}.pid").toString()
            }
            storage {
                dbPath = baseDir.getAbsolutePath()
            }
            systemLog {
                destination = FILE
                path = "${baseDir}/mongo.log"
            }
        }
    }
}