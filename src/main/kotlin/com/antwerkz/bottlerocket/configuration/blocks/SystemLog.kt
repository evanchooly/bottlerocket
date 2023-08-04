package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.types.Destination
import com.antwerkz.bottlerocket.configuration.types.RotateBehavior
import com.antwerkz.bottlerocket.configuration.types.TimestampFormat
import com.antwerkz.bottlerocket.configuration.types.Verbosity

class SystemLog(
    var verbosity: Verbosity? = null,
    var quiet: Boolean? = null,
    var traceAllExceptions: Boolean? = null,
    var syslogFacility: String? = null,
    var path: String? = null,
    var logAppend: Boolean? = null,
    var logRotate: RotateBehavior? = null,
    var destination: Destination? = null,
    var timeStampFormat: TimestampFormat? = null,
    var component: Component? = null
) : ConfigBlock {
    fun component(init: Component.() -> Unit) {
        component = initConfigBlock(Component(), init)
    }
}
