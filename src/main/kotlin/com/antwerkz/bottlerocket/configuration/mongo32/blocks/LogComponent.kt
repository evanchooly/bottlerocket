package com.antwerkz.bottlerocket.configuration.mongo32.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.ConfigMode
import com.antwerkz.bottlerocket.configuration.types.Verbosity
import com.github.zafarkhaja.semver.Version

class LogComponent {
    class AccessControl(var verbosity: Verbosity? = null) : ConfigBlock

    class Command(var verbosity: Verbosity? = null) : ConfigBlock

    class Control(var verbosity: Verbosity? = null) : ConfigBlock

    class Ftdc(var verbosity: Verbosity? = null) : ConfigBlock

    class Geo(var verbosity: Verbosity? = null) : ConfigBlock

    class Index(var verbosity: Verbosity? = null) : ConfigBlock

    class Network(var verbosity: Verbosity? = null) : ConfigBlock

    class Query(var verbosity: Verbosity? = null) : ConfigBlock

    class Replication(var verbosity: Verbosity? = null) : ConfigBlock

    class Sharding(var verbosity: Verbosity? = null) : ConfigBlock

    class Storage(var verbosity: Verbosity? = null,
                  var journal: Journal = Journal()) : ConfigBlock {
        fun journal(init: Journal.() -> Unit) {
            journal = initConfigBlock(Journal(), init)
        }

    }

    class Journal(var verbosity: Verbosity? = null) : ConfigBlock

    class Write(var verbosity: Verbosity? = null) : ConfigBlock
}
