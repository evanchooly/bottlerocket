package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.ConfigMode
import com.antwerkz.bottlerocket.configuration.Since
import com.github.zafarkhaja.semver.Version

class Component(
      Since("3.0.0") var accessControl: LogComponent.AccessControl = LogComponent.AccessControl(),
      Since("3.0.0") var command: LogComponent.Command = LogComponent.Command(),
      Since("3.0.0") var control: LogComponent.Control = LogComponent.Control(),
      Since("3.0.0") var geo: LogComponent.Geo = LogComponent.Geo(),
      Since("3.0.0") var index: LogComponent.Index = LogComponent.Index(),
      Since("3.0.0") var network: LogComponent.Network = LogComponent.Network(),
      Since("3.0.0") var query: LogComponent.Query = LogComponent.Query(),
      Since("3.0.0") var replication: LogComponent.Replication = LogComponent.Replication(),
      Since("3.0.0") var sharding: LogComponent.Sharding = LogComponent.Sharding(),
      Since("3.0.0") var storage: LogComponent.Storage = LogComponent.Storage(),
//      Since("3.0.0") var storageJournal: LogComponent.StorageJournal = LogComponent.StorageJournal(),
      Since("3.0.0") var write: LogComponent.Write = LogComponent.Write()
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