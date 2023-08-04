package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.Added
import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.types.FreeMonitoringState

class Free(
    @Added("4.0.0") var state: FreeMonitoringState? = null,
    @Added("4.0.0") var tags: String? = null
) : ConfigBlock
