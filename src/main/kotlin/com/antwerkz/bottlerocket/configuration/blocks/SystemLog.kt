package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.*

class SystemLog(
      Since("3.0") var verbosity: Verbosity = Verbosity.ZERO,
      var component: Component = Component(),
      var quiet: Boolean = false,
      var traceAllExceptions: Boolean = false,
      var syslogFacility: String = "user",
      var path: String? = null,
      var logAppend: Boolean = false,
      Since("3.0") var logRotate: RotateBehavior = RotateBehavior.RENAME,
      public var destination: Destination = Destination.STANDARD_OUT,
      var timeStampFormat: TimestampFormat = TimestampFormat.ISO8601_LOCAL
) : ConfigBlock {
    fun component(init: Component.() -> Unit) {
        component = initConfigBlock(Component(), init)
    }
}