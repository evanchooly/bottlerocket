package com.antwerkz.bottlerocket.configuration.mongo30.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.ConfigMode
import com.antwerkz.bottlerocket.configuration.Mode

class Mmapv1() : ConfigBlock {
    @Mode(ConfigMode.MONGOD) var preallocDataFiles: Boolean? = false
    @Mode(ConfigMode.MONGOD) var nsSize: Int? = null
    var quota: Mmapv1.Quota = Mmapv1.Quota()
    @Mode(ConfigMode.MONGOD) var smallFiles: Boolean? = true
    var journal: Mmapv1.Journal = Mmapv1.Journal()

    fun quota(init: Mmapv1.Quota.() -> Unit) {
        quota = initConfigBlock(Mmapv1.Quota(), init)
    }

    fun journal(init: Mmapv1.Journal.() -> Unit) {
        journal = initConfigBlock(Mmapv1.Journal(), init)
    }

    class Quota() : ConfigBlock {
        @Mode(ConfigMode.MONGOD) var enforced: Boolean? = null
        @Mode(ConfigMode.MONGOD) var maxFilesPerDB: Int? = null
    }

    class Journal() : ConfigBlock {
        @Mode(ConfigMode.MONGOD) var debugFlags: Int? = null
        @Mode(ConfigMode.MONGOD) var commitIntervalMs: Int? = null
    }
}