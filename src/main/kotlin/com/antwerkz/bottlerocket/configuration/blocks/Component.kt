package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.blocks.LogComponents.AccessControl
import com.antwerkz.bottlerocket.configuration.blocks.LogComponents.Command
import com.antwerkz.bottlerocket.configuration.blocks.LogComponents.Control
import com.antwerkz.bottlerocket.configuration.blocks.LogComponents.Ftdc
import com.antwerkz.bottlerocket.configuration.blocks.LogComponents.Geo
import com.antwerkz.bottlerocket.configuration.blocks.LogComponents.Index
import com.antwerkz.bottlerocket.configuration.blocks.LogComponents.Network
import com.antwerkz.bottlerocket.configuration.blocks.LogComponents.Query
import com.antwerkz.bottlerocket.configuration.blocks.LogComponents.Replication
import com.antwerkz.bottlerocket.configuration.blocks.LogComponents.Sharding
import com.antwerkz.bottlerocket.configuration.blocks.LogComponents.Storage
import com.antwerkz.bottlerocket.configuration.blocks.LogComponents.Transaction
import com.antwerkz.bottlerocket.configuration.blocks.LogComponents.Write

class Component(
    var accessControl: AccessControl = AccessControl(),
    var command: Command? = null,
    var control: Control? = null,
    var ftdc: Ftdc? = null,
    var geo: Geo? = null,
    var index: Index? = null,
    var network: Network? = null,
    var query: Query? = null,
    var replication: Replication? = null,
    var sharding: Sharding? = null,
    var storage: Storage? = null,
    var transaction: Transaction? = null,
    var write: Write? = null
) : ConfigBlock {
    fun accessControl(init: AccessControl.() -> Unit) {
        accessControl = initConfigBlock(AccessControl(), init)
    }

    fun command(init: Command.() -> Unit) {
        command = initConfigBlock(Command(), init)
    }

    fun control(init: Control.() -> Unit) {
        control = initConfigBlock(Control(), init)
    }

    fun ftdc(init: Ftdc.() -> Unit) {
        ftdc = initConfigBlock(Ftdc(), init)
    }

    fun geo(init: Geo.() -> Unit) {
        geo = initConfigBlock(Geo(), init)
    }

    fun index(init: Index.() -> Unit) {
        index = initConfigBlock(Index(), init)
    }

    fun network(init: Network.() -> Unit) {
        network = initConfigBlock(Network(), init)
    }

    fun query(init: Query.() -> Unit) {
        query = initConfigBlock(Query(), init)
    }

    fun replication(init: Replication.() -> Unit) {
        replication = initConfigBlock(Replication(), init)
    }

    fun sharding(init: Sharding.() -> Unit) {
        sharding = initConfigBlock(Sharding(), init)
    }

    fun storage(init: Storage.() -> Unit) {
        storage = initConfigBlock(Storage(), init)
    }

    fun transaction(init: Transaction.() -> Unit) {
        transaction = initConfigBlock(Transaction(), init)
    }

    fun write(init: Write.() -> Unit) {
        write = initConfigBlock(Write(), init)
    }
}