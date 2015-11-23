package com.antwerkz.bottlerocket.configuration.mongo30.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.ConfigMode
import com.antwerkz.bottlerocket.configuration.Mode

class Storage(
      @Mode(ConfigMode.MONGOD) var dbPath: String? = null,
      @Mode(ConfigMode.MONGOD) var directoryPerDB: Boolean? = null,
      @Mode(ConfigMode.MONGOD) var indexBuildRetry: Boolean? = null,
      @Mode(ConfigMode.MONGOD) var repairPath: String? = null,
      @Mode(ConfigMode.MONGOD) var syncPeriodSecs: Int? = null,
      var engine: String? = null,
      @Mode(ConfigMode.MONGOD) var journal: Journal = Journal(),
      var mmapv1: Mmapv1 = Mmapv1(),
      var wiredTiger: WiredTiger = WiredTiger()
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