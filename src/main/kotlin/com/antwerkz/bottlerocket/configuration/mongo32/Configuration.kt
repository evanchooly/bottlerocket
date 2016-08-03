package com.antwerkz.bottlerocket.configuration.mongo32

import com.antwerkz.bottlerocket.configuration.mongo32.blocks.AuditLog
import com.antwerkz.bottlerocket.configuration.mongo32.blocks.BasisTech
import com.antwerkz.bottlerocket.configuration.mongo32.blocks.Net
import com.antwerkz.bottlerocket.configuration.mongo32.blocks.OperationProfiling
import com.antwerkz.bottlerocket.configuration.mongo32.blocks.ProcessManagement
import com.antwerkz.bottlerocket.configuration.mongo32.blocks.Replication
import com.antwerkz.bottlerocket.configuration.mongo32.blocks.Security
import com.antwerkz.bottlerocket.configuration.mongo32.blocks.Sharding
import com.antwerkz.bottlerocket.configuration.mongo32.blocks.Snmp
import com.antwerkz.bottlerocket.configuration.mongo32.blocks.Storage
import com.antwerkz.bottlerocket.configuration.mongo32.blocks.SystemLog
import com.antwerkz.bottlerocket.configuration.types.State

/**
 * @see http://docs.mongodb.org/v3.0/reference/configuration-options/
 */
class Configuration(
        var auditLog: AuditLog = AuditLog(),
        var basisTech: BasisTech = BasisTech(),
        var net: Net = Net(),
        var operationProfiling: OperationProfiling = OperationProfiling(),
        var processManagement: ProcessManagement = ProcessManagement(),
        var replication: Replication = Replication(),
        var security: Security = Security(),
        var sharding: Sharding = Sharding(),
        var snmp: Snmp = Snmp(),
        var storage: Storage = Storage(),
        var systemLog: SystemLog = SystemLog()
) : com.antwerkz.bottlerocket.configuration.Configuration {
    override fun isAuthEnabled(): Boolean {
        return security.authorization == State.ENABLED || security.keyFile != null
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

    fun basisTech(init: BasisTech.() -> Unit) {
        basisTech = initConfigBlock(BasisTech(), init)
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
