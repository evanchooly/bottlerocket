package com.antwerkz.bottlerocket.configuration

import com.antwerkz.bottlerocket.configuration.blocks.*
import com.github.zafarkhaja.semver.Version
import kotlin.platform.platformStatic

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
    @override
    fun toYaml(version: Version, mode: ConfigMode, includeAll: Boolean): String {
        return Configuration::class.properties
              .map { (it.get(this) as ConfigBlock).toYaml(version, mode, includeAll) }
              .filter { it != "" }
              .toList()
              .join("\n")
    }

    @override
    fun toProperties(version: Version, mode: ConfigMode, includeAll: Boolean): Map<String, Any> {
        var map = linkedMapOf<String, Any>()
        Configuration::class.properties
              .map { (it.get(this) as ConfigBlock).toProperties(version, mode, includeAll) }
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
