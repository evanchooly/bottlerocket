package com.antwerkz.bottlerocket.configuration.mongo30.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.ConfigMode
import com.github.zafarkhaja.semver.Version

class Component(
      var accessControl: LogComponent.AccessControl = LogComponent.AccessControl(),
      var command: LogComponent.Command = LogComponent.Command(),
      var control: LogComponent.Control = LogComponent.Control(),
      var geo: LogComponent.Geo = LogComponent.Geo(),
      var index: LogComponent.Index = LogComponent.Index(),
      var network: LogComponent.Network = LogComponent.Network(),
      var query: LogComponent.Query = LogComponent.Query(),
      var replication: LogComponent.Replication = LogComponent.Replication(),
      var sharding: LogComponent.Sharding = LogComponent.Sharding(),
      var storage: LogComponent.Storage = LogComponent.Storage(),
      var write: LogComponent.Write = LogComponent.Write()
) : ConfigBlock {
    fun accessControl(init: LogComponent.AccessControl.() -> Unit) {
        accessControl = initConfigBlock(LogComponent.AccessControl(), init)
    }

    fun command(init: LogComponent.Command.() -> Unit) {
        command = initConfigBlock(LogComponent.Command(), init)
    }

    fun control(init: LogComponent.Control.() -> Unit) {
        control = initConfigBlock(LogComponent.Control(), init)
    }

    fun geo(init: LogComponent.Geo.() -> Unit) {
        geo = initConfigBlock(LogComponent.Geo(), init)
    }

    fun index(init: LogComponent.Index.() -> Unit) {
        index = initConfigBlock(LogComponent.Index(), init)
    }

    fun network(init: LogComponent.Network.() -> Unit) {
        network = initConfigBlock(LogComponent.Network(), init)
    }

    fun query(init: LogComponent.Query.() -> Unit) {
        query = initConfigBlock(LogComponent.Query(), init)
    }

    fun replication(init: LogComponent.Replication.() -> Unit) {
        replication = initConfigBlock(LogComponent.Replication(), init)
    }

    fun sharding(init: LogComponent.Sharding.() -> Unit) {
        sharding = initConfigBlock(LogComponent.Sharding(), init)
    }

    fun storage(init: LogComponent.Storage.() -> Unit) {
        storage = initConfigBlock(LogComponent.Storage(), init)
    }

    fun write(init: LogComponent.Write.() -> Unit) {
        write = initConfigBlock(LogComponent.Write(), init)
    }
}