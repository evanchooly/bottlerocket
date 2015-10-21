package com.antwerkz.bottlerocket.configuration.mongo30.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.types.Verbosity

class LogComponent {
    class AccessControl() : ConfigBlock {
        var verbosity: Verbosity? = null
    }

    class Command() : ConfigBlock {
        var verbosity: Verbosity? = null
    }

    class Control() : ConfigBlock {
        var verbosity: Verbosity? = null
    }

    class Geo() : ConfigBlock {
        var verbosity: Verbosity? = null
    }

    class Index() : ConfigBlock {
        var verbosity: Verbosity? = null
    }

    class Network() : ConfigBlock {
        var verbosity: Verbosity? = null
    }

    class Query() : ConfigBlock {
        var verbosity: Verbosity? = null
    }

    class Replication() : ConfigBlock {
        var verbosity: Verbosity? = null
    }

    class Sharding() : ConfigBlock {
        var verbosity: Verbosity? = null
    }

    class Storage() : ConfigBlock {
        var verbosity: Verbosity? = null
        var journal: Journal = Journal()
        fun journal(init: Journal.() -> Unit) {
            journal = initConfigBlock(Journal(), init)
        }

    }

    class Journal() : ConfigBlock {
        var verbosity: Verbosity? = null
    }

    class Write() : ConfigBlock {
        var verbosity: Verbosity? = null
    }
}
