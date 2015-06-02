package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.*

class Storage(
      var dbPath: String = "/data/db",
      var indexBuildRetry: Boolean = true,
      var repairPath: String = dbPath + "_tmp",
      var journal: Journal = Journal(),
      var directoryPerDB: Boolean = false,
      var syncPeriodSecs: Int = 60,
      Since("3.0") var engine: String = "mmapv1",
      var mmapv1: Mmapv1 = Mmapv1(),
      Since("3.0.0") var wiredTiger: WiredTiger = WiredTiger()
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