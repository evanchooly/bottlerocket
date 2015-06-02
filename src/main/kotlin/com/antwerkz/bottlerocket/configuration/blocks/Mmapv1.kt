package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock

class Mmapv1(
      var preallocDataFiles: Boolean = true,
      var nsSize: Int = 16,
      var quota: Mmapv1.Quota = Mmapv1.Quota(),
      var smallFiles: Boolean = true,
      var journal: Mmapv1.Journal = Mmapv1.Journal()
) : ConfigBlock {

    fun quota(init: Quota.() -> Unit) {
        quota = initConfigBlock(Quota(), init)
    }

    fun journal(init: Journal.() -> Unit) {
        journal = initConfigBlock(Journal(), init)
    }

    class Quota(
          var enforced: Boolean = false,
          var maxFilesPerDB: Int = 8
    ) : ConfigBlock

    class Journal(
          var debugFlags: Int = 0,
          var commitIntervalMs: Int = 100
    ) : ConfigBlock

}