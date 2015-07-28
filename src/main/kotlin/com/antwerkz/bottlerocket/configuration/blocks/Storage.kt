package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.*

class Storage(
      @Mode(ConfigMode.MONGOD) var dbPath: String = "/data/db",
      @Mode(ConfigMode.MONGOD) var indexBuildRetry: Boolean = true,
      @Mode(ConfigMode.MONGOD) var repairPath: String = dbPath + "_tmp",
      @Mode(ConfigMode.MONGOD) var journal: Journal = Journal(),
      @Mode(ConfigMode.MONGOD) var directoryPerDB: Boolean = false,
      @Mode(ConfigMode.MONGOD) var syncPeriodSecs: Int = 60,
      @Since("3.0.0") var engine: String = "mmapv1",
      var mmapv1: Mmapv1 = Mmapv1(),
      @Since("3.0.0") var wiredTiger: WiredTiger = WiredTiger()
) : ConfigBlock {

    fun journal(init: Journal.() -> Unit) {
        journal = initConfigBlock(Journal(), init)
    }

    fun mmapv1(init: Mmapv1.() -> Unit) {
        mmapv1 = initConfigBlock(Mmapv1(), init)
    }

    fun wiredTiger(init: WiredTiger.() -> Unit) {
        wiredTiger = initConfigBlock(WiredTiger(), init)
    }
}