package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock

class Monitoring(
    var free: Free = Free()
) : ConfigBlock