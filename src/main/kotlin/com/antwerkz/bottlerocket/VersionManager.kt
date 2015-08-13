package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.configuration.ConfigMode
import com.antwerkz.bottlerocket.configuration.Configuration
import com.antwerkz.bottlerocket.configuration.mongo22.VersionManager22
import com.antwerkz.bottlerocket.configuration.mongo24.VersionManager24
import com.antwerkz.bottlerocket.configuration.mongo26.VersionManager26
import com.antwerkz.bottlerocket.configuration.mongo30.VersionManager30
import com.antwerkz.bottlerocket.executable.Mongod
import com.github.zafarkhaja.semver.Version
import com.mongodb.ReadPreference
import org.bson.Document
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.util.ArrayList

interface VersionManager {
    val version: Version
    fun writeConfig(configFile: File, config: Configuration, mode: ConfigMode = ConfigMode.MONGOD)
    fun initialConfig(baseDir: File, name: String, port: Int): Configuration

    fun initialize(replicaSet: ReplicaSet)
    fun getReplicaSetConfig(primary: Mongod): Document
    fun setReplicaSetName(node: Mongod, name: String)
}

abstract class BaseVersionManager(override val version: Version) : VersionManager {
    companion object {
        val LOG: Logger = LoggerFactory.getLogger(javaClass<BaseVersionManager>())

        fun of(version: Version): VersionManager {
            val s = version.getNormalVersion()
            return when (s.substring(0, s.lastIndexOf('.'))) {
                "3.0" -> VersionManager30(version);
                "2.6" -> VersionManager26(version)
                "2.4" -> VersionManager24(version)
                "2.2" -> VersionManager22(version)
                else -> throw RuntimeException(version.toString());
            }
        }
    }

    override fun writeConfig(configFile: File, config: Configuration, mode: ConfigMode) {
        configFile.writeText(config.toYaml(mode))
    }

    fun initiateReplicaSet(replicaSet: ReplicaSet) {
        val primary = replicaSet.nodes.first()
        val results = primary.runCommand(Document("replSetInitiate",
              Document("_id", replicaSet.name)
                    .append("members", listOf(Document("_id", 1)
                          .append("host", "localhost:${primary.port}"))
                    )), ReadPreference.primaryPreferred())
        if ( !(results.getDouble("ok")?.toInt()?.equals(1) ?: false) ) {
            throw IllegalStateException("Failed to initiate replica set: ${results}")
        }
    }

    override fun initialize(replicaSet: ReplicaSet) {
        if (replicaSet.initialized) {
            return;
        }

        initiateReplicaSet(replicaSet)
        BaseVersionManager.LOG.info("replSet initiated.  waiting for primary.")
        replicaSet.waitForPrimary()
        BaseVersionManager.LOG.info("primary found.  adding other members.")
        addMembers(replicaSet, replicaSet.nodes.asSequence().withIndex()
              .filter({ it.index > 0 })
              .map { it.value })

        replicaSet.waitForPrimary()
        replicaSet.initialized = true;

        BaseVersionManager.LOG.info("replica set ${replicaSet.name} started.")
    }

    private fun addMembers(replicaSet: ReplicaSet, lisdt: Sequence<Mongod>) {
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
}

