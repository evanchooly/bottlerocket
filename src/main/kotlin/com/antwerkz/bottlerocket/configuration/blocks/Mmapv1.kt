package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.ConfigMode
import com.antwerkz.bottlerocket.configuration.Mode

class Mmapv1(
      @Mode(ConfigMode.MONGOD) var preallocDataFiles: Boolean? = false,
      @Mode(ConfigMode.MONGOD) var nsSize: Int? = null,
      var quota: Mmapv1.Quota = Mmapv1.Quota(),
      @Mode(ConfigMode.MONGOD) var smallFiles: Boolean? = true,
      var journal: Mmapv1.Journal = Mmapv1.Journal()
) : ConfigBlock {

    fun quota(init: Quota.() -> Unit) {
        quota = initConfigBlock(Quota(), init)
    }

    fun journal(init: Journal.() -> Unit) {
        journal = initConfigBlock(Journal(), init)
    }

    class Quota(
          @Mode(ConfigMode.MONGOD) var enforced: Boolean? = null,
          @Mode(ConfigMode.MONGOD) var maxFilesPerDB: Int? = null
    ) : ConfigBlock

    class Journal(
          @Mode(ConfigMode.MONGOD) var debugFlags: Int? = null,
          @Mode(ConfigMode.MONGOD) var commitIntervalMs: Int? = null
    ) : ConfigBlock

}