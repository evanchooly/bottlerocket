package com.antwerkz.bottlerocket.configuration

import com.antwerkz.bottlerocket.configuration.blocks.AuditLog
import com.antwerkz.bottlerocket.configuration.blocks.Cloud
import com.antwerkz.bottlerocket.configuration.blocks.Net
import com.antwerkz.bottlerocket.configuration.blocks.OperationProfiling
import com.antwerkz.bottlerocket.configuration.blocks.ProcessManagement
import com.antwerkz.bottlerocket.configuration.blocks.Replication
import com.antwerkz.bottlerocket.configuration.blocks.Security
import com.antwerkz.bottlerocket.configuration.blocks.SetParameter
import com.antwerkz.bottlerocket.configuration.blocks.Sharding
import com.antwerkz.bottlerocket.configuration.blocks.Snmp
import com.antwerkz.bottlerocket.configuration.blocks.Storage
import com.antwerkz.bottlerocket.configuration.blocks.SystemLog
import com.antwerkz.bottlerocket.configuration.types.State

class Configuration(
    var auditLog: AuditLog = AuditLog(),
    var cloud: Cloud = Cloud(),
    var net: Net = Net(),
    var operationProfiling: OperationProfiling = OperationProfiling(),
    var processManagement: ProcessManagement = ProcessManagement(),
    var replication: Replication = Replication(),
    var security: Security = Security(),
    var sharding: Sharding = Sharding(),
    var snmp: Snmp = Snmp(),
    var storage: Storage = Storage(),
    var systemLog: SystemLog = SystemLog(),
    var setParameter: SetParameter = SetParameter()
) : ConfigBlock {
    fun isAuthEnabled(): Boolean {
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

    fun cloud(init: Cloud.() -> Unit) {
        cloud = initConfigBlock(Cloud(), init)
    }

    fun snmp(init: Snmp.() -> Unit) {
        snmp = initConfigBlock(Snmp(), init)
    }
    fun setParameter(init: SetParameter.() -> Unit) {
        setParameter = initConfigBlock(SetParameter(), init)
    }

    override fun nodeName(): String {
        return "configuration"
    }

    override fun toMap(mode: ConfigMode, includeAll: Boolean): Map<String, Any> {
        return super.toMap(mode, includeAll).get("configuration") as Map<String, Any>
    }
}

fun configuration(init: Configuration.() -> Unit): Configuration {
    val configuration = Configuration()
    configuration.init()
    return configuration
}
