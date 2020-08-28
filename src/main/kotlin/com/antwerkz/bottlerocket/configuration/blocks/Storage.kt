package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.Added
import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.ConfigMode
import com.antwerkz.bottlerocket.configuration.Mode
import com.antwerkz.bottlerocket.configuration.Removed

class Storage(
    @Mode(ConfigMode.MONGOD) var dbPath: String? = null,
    @Mode(ConfigMode.MONGOD) var directoryPerDB: Boolean? = null,
    @Removed("4.4.0")
    @Mode(ConfigMode.MONGOD) var indexBuildRetry: Boolean? = null,
    @Removed("4.2.0")
    @Mode(ConfigMode.MONGOD) var repairPath: String? = null,
    @Mode(ConfigMode.MONGOD) var syncPeriodSecs: Int? = null,
    var engine: String? = null,
    @Mode(ConfigMode.MONGOD) var journal: Journal? = null,
    var mmapv1: Mmapv1? = null,
    var inMemory: InMemory? = null,
    var wiredTiger: WiredTiger? = null,
    @Added("4.4.0")
    var oplogMinRetentionHours: Int? = null
) : ConfigBlock {
    fun journal(init: Journal.() -> Unit) {
        journal = initConfigBlock(Journal(), init)
    }

    fun mmapv1(init: Mmapv1.() -> Unit) {
        mmapv1 = initConfigBlock(Mmapv1(), init)
    }

    fun inMemory(init: InMemory.() -> Unit) {
        inMemory = initConfigBlock(InMemory(), init)
    }

    fun wiredTiger(init: WiredTiger.() -> Unit) {
        wiredTiger = initConfigBlock(WiredTiger(), init)
    }
}