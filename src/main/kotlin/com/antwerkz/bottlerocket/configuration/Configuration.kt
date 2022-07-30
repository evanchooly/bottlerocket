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
import java.io.File
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

@Suppress("UNCHECKED_CAST", "unused", "MemberVisibilityCanBePrivate")
class Configuration(
    var auditLog: AuditLog? = null,
    var cloud: Cloud? = null,
    var net: Net? = null,
    var operationProfiling: OperationProfiling? = null,
    var processManagement: ProcessManagement? = null,
    var replication: Replication? = null,
    var security: Security? = null,
    var sharding: Sharding? = null,
    var snmp: Snmp? = null,
    var storage: Storage? = null,
    var systemLog: SystemLog? = null,
    var setParameter: SetParameter? = null
) : ConfigBlock {
    fun writeConfig(configFile: File, mode: ConfigMode) {
        configFile.parentFile.mkdirs()
        configFile.writeText(toYaml(mode))
    }

    fun update(updates: Configuration.() -> Unit): Configuration {
        val configuration = Configuration()
        configuration.updates()

        return update(configuration)
    }

    fun update(updates: Configuration): Configuration {
        val target = empty<Configuration>()
        return target
            .mergeValues(this)
            .mergeValues(updates) as Configuration
    }

    private fun <T : ConfigBlock> ConfigBlock.empty(): T {
        return this::class.primaryConstructor!!.callBy(mapOf()) as T
    }

    private fun ConfigBlock.mergeValues(source: ConfigBlock): ConfigBlock {
        source.javaClass.kotlin.memberProperties
            .forEach { p: KProperty1<ConfigBlock, *> ->
                var fieldValue = p.get(source)
                if (fieldValue != null) {
                    if (fieldValue is ConfigBlock) {
                        fieldValue = fieldValue.empty<ConfigBlock>()
                            .mergeValues(fieldValue)
                    }
                    (p as KMutableProperty<*>).setter.call(this, fieldValue)
                }
            }
        return this
    }

    fun isAuthEnabled(): Boolean {
        return security?.authorization == State.ENABLED || security?.keyFile != null
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
