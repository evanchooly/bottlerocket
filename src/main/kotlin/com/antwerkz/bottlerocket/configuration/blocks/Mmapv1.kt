package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.ConfigMode
import com.antwerkz.bottlerocket.configuration.Mode
import com.antwerkz.bottlerocket.configuration.Removed

class Mmapv1(
    @Removed("4.2.0")
    @Mode(ConfigMode.MONGOD) var preallocDataFiles: Boolean? = null,
    @Removed("4.2.0")
    @Mode(ConfigMode.MONGOD) var nsSize: Int? = null,
    var quota: Quota? = null,
    @Removed("4.2.0")
    @Mode(ConfigMode.MONGOD) var smallFiles: Boolean? = null,
    var journal: Journal? = null
) : ConfigBlock {
    fun quota(init: Quota.() -> Unit) {
        quota = initConfigBlock(Quota(), init)
    }

    fun journal(init: Journal.() -> Unit) {
        journal = initConfigBlock(Journal(), init)
    }

    class Quota(
        @Removed("4.2.0")
        @Mode(ConfigMode.MONGOD) var enforced: Boolean? = null,
        @Removed("4.2.0")
        @Mode(ConfigMode.MONGOD) var maxFilesPerDB: Int? = null
    ) : ConfigBlock

    class Journal(
        @Removed("4.2.0")
        @Mode(ConfigMode.MONGOD) var debugFlags: Int? = null,
        @Removed("4.2.0")
        @Mode(ConfigMode.MONGOD) var commitIntervalMs: Int? = null
    ) : ConfigBlock
}