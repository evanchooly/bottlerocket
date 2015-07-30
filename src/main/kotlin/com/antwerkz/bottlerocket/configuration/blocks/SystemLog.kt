package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.*
import com.github.zafarkhaja.semver.Version

class SystemLog(
      @Since("3.0.0") var verbosity: Verbosity? = null,
      var quiet: Boolean? = null,
      var traceAllExceptions: Boolean? = null,
      var syslogFacility: String? = null,
      var path: String? = null,
      var logAppend: Boolean? = null,
      @Since("3.0.0") var logRotate: RotateBehavior? = null,
      var destination: Destination? = null,
      var timeStampFormat: TimestampFormat? = null,
      var component: Component = Component()
) : ConfigBlock {
    fun component(init: Component.() -> Unit) {
        component = initConfigBlock(Component(), init)
    }
}