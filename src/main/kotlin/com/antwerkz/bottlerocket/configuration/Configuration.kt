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

/**
 * @see http://docs.mongodb.org/manual/reference/configuration-options/
 */
class Configuration(
      var systemLog: SystemLog = SystemLog(),
      var processManagement: ProcessManagement = ProcessManagement(),
      var net: Net = Net(),
      var security: Security = Security(),
      var operationProfiling: OperationProfiling = OperationProfiling(),
      var storage: Storage = Storage(),
      var replication: Replication = Replication(),
      var sharding: Sharding = Sharding(),
      var auditLog: AuditLog = AuditLog(),
      var snmp: Snmp = Snmp()
) : ConfigBlock {
    override fun toYaml(version: Version, mode: ConfigMode): String {
        return Configuration::class.properties
              .map { (it.get(this) as ConfigBlock).toYaml(version, mode) }
              .filter { it != "" }
              .toList()
              .join("\n")
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
