package com.antwerkz.bottlerocket.configuration.mongo26.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.ConfigMode
import com.antwerkz.bottlerocket.configuration.Mode

class Storage() : ConfigBlock {
    @Mode(ConfigMode.MONGOD) var dbPath: String? = null
    @Mode(ConfigMode.MONGOD) var directoryPerDB: Boolean? = null
    @Mode(ConfigMode.MONGOD) var indexBuildRetry: Boolean? = null
    @Mode(ConfigMode.MONGOD) var nsSize: Int? = null
    @Mode(ConfigMode.MONGOD) var preallocDataFiles: String? = null
    @Mode(ConfigMode.MONGOD) var repairPath: String? = null
    @Mode(ConfigMode.MONGOD) var smallFiles: Boolean? = true
    @Mode(ConfigMode.MONGOD) var syncPeriodSecs: Int? = null
    @Mode(ConfigMode.MONGOD) var journal: Journal = Journal()
    @Mode(ConfigMode.MONGOD) var quota: Quota = Quota()

    fun quota(init: Quota.() -> Unit) {
        quota = initConfigBlock(Quota(), init)
    }

    fun journal(init: Journal.() -> Unit) {
        journal = initConfigBlock(Journal(), init)
    }
}