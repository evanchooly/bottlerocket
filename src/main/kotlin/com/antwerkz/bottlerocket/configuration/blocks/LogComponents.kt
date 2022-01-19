package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.Added
import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.types.Verbosity

class LogComponents : ConfigBlock {
    class AccessControl(var verbosity: Verbosity? = null) : ConfigBlock
    class Command(var verbosity: Verbosity? = null) : ConfigBlock
    class Control(var verbosity: Verbosity? = null) : ConfigBlock
    class Ftdc(var verbosity: Verbosity? = null) : ConfigBlock
    class Geo(var verbosity: Verbosity? = null) : ConfigBlock
    class Heartbeats(var verbosity: Int? = null) : ConfigBlock
    class Index(var verbosity: Verbosity? = null) : ConfigBlock
    class Network(var verbosity: Verbosity? = null) : ConfigBlock
    class Query(var verbosity: Verbosity? = null) : ConfigBlock
    class Replication(
        var verbosity: Verbosity? = null,
        var election: Election? = null,
        var initialSync: InitialSync? = null,
        var heartbeats: Heartbeats? = null,
        var rollback: Rollback? = null
    ) : ConfigBlock {
        fun rollback(init: Rollback.() -> Unit) {
            rollback = initConfigBlock(Rollback(), init)
        }

        fun heartbeats(init: Heartbeats.() -> Unit) {
            heartbeats = initConfigBlock(Heartbeats(), init)
        }
    }

    class Rollback(var verbosity: Int? = null) : ConfigBlock
    class Election(@Added("4.2.0") var verbosity: Int? = null) : ConfigBlock
    class InitialSync(@Added("4.2.0") var verbosity: Int? = null) : ConfigBlock
    class Sharding(var verbosity: Verbosity? = null) : ConfigBlock
    class Storage(
        var verbosity: Verbosity? = null,
        var journal: Journal? = null,
        var recovery: Recovery? = null
    ) : ConfigBlock {
        fun journal(init: Journal.() -> Unit) {
            journal = initConfigBlock(Journal(), init)
        }

        fun recovery(init: Recovery.() -> Unit) {
            recovery = initConfigBlock(Recovery(), init)
        }
    }

    class Transaction(@Added("4.0.0") var verbosity: Verbosity? = null) : ConfigBlock
    class Journal(var verbosity: Verbosity? = null) : ConfigBlock
    class Recovery(@Added("4.0.0") var verbosity: Verbosity? = null) : ConfigBlock
    class Write(var verbosity: Verbosity? = null) : ConfigBlock
}
