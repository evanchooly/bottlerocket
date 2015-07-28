package com.antwerkz.bottlerocket.configuration

import com.antwerkz.bottlerocket.configuration.blocks.AuditLog
import com.antwerkz.bottlerocket.configuration.blocks.Net
import com.antwerkz.bottlerocket.configuration.blocks.OperationProfiling
import com.antwerkz.bottlerocket.configuration.blocks.ProcessManagement
import com.antwerkz.bottlerocket.configuration.blocks.Replication
import com.antwerkz.bottlerocket.configuration.blocks.Security
import com.antwerkz.bottlerocket.configuration.blocks.Sharding
import com.antwerkz.bottlerocket.configuration.blocks.Snmp
import com.antwerkz.bottlerocket.configuration.blocks.Storage
import com.antwerkz.bottlerocket.configuration.blocks.SystemLog
import com.github.zafarkhaja.semver.Version
import java.util.*

/**
 * @see http://docs.mongodb.org/manual/reference/configuration-options/
 */
class Configuration(
      var auditLog: AuditLog = AuditLog(),
      var net: Net = Net(),
      var operationProfiling: OperationProfiling = OperationProfiling(),
      var processManagement: ProcessManagement = ProcessManagement(),
      var replication: Replication = Replication(),
      var security: Security = Security(),
      var sharding: Sharding = Sharding(),
      var snmp: Snmp = Snmp(),
      var storage: Storage = Storage(),
      var systemLog: SystemLog = SystemLog()
) : ConfigBlock {
    override fun toYaml(version: Version, mode: ConfigMode, omitDefaults: Boolean): String {
        return Configuration::class.properties
              .map { (it.get(this) as ConfigBlock).toYaml(version, mode, omitDefaults) }
              .filter { it != "" }
              .toList()
              .join("\n")
    }

    override fun toProperties(version: Version, mode: ConfigMode, omitDefaults: Boolean): Map<String, Any> {
        var map = linkedMapOf<String, Any>()
        Configuration::class.properties
              .map { (it.get(this) as ConfigBlock).toProperties(version, mode, omitDefaults) }
              .forEach { map.putAll(it) }
        return map
    }

    fun systemLog(init: SystemLog.() -> Unit) {
        systemLog = initConfigBlock(SystemLog(), init)
    }

    fun processManagement(init: ProcessManagement.() -> Unit) {
        processManagement = initConfigBlock(ProcessManagement(), init)
    }

    fun net(init: Net.() -> Unit) {
        net = initConfigBlock(Net(), init)
    }

    fun security(init: Security.() -> Unit) {
        security = initConfigBlock(Security(), init)
    }

    fun operationProfiling(init: OperationProfiling.() -> Unit) {
        operationProfiling = initConfigBlock(OperationProfiling(), init)
    }

    fun storage(init: Storage.() -> Unit) {
        storage = initConfigBlock(Storage(), init)
    }

    fun replication(init: Replication.() -> Unit) {
        replication = initConfigBlock(Replication(), init)
    }

    fun sharding(init: Sharding.() -> Unit) {
        sharding = initConfigBlock(Sharding(), init)
    }

    fun auditLog(init: AuditLog.() -> Unit) {
        auditLog = initConfigBlock(AuditLog(), init)
    }

    fun snmp(init: Snmp.() -> Unit) {
        snmp = initConfigBlock(Snmp(), init)
    }
}


fun configuration(init: Configuration.() -> Unit): Configuration {
    val configuration = Configuration()
    configuration.init()
    return configuration
}
