package com.antwerkz.bottlerocket.configuration.blocks

import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.Verbosity

class LogComponent {
    class AccessControl(var verbosity: Verbosity = Verbosity.ZERO) : ConfigBlock

    class Command(var verbosity: Verbosity = Verbosity.ZERO) : ConfigBlock

    class Control(var verbosity: Verbosity = Verbosity.ZERO) : ConfigBlock

    class Geo(var verbosity: Verbosity = Verbosity.ZERO) : ConfigBlock

    class Index(var verbosity: Verbosity = Verbosity.ZERO) : ConfigBlock

    class Network(var verbosity: Verbosity = Verbosity.ZERO) : ConfigBlock

    class Query(var verbosity: Verbosity = Verbosity.ZERO) : ConfigBlock

    class Replication(var verbosity: Verbosity = Verbosity.ZERO) : ConfigBlock

    class Sharding(var verbosity: Verbosity = Verbosity.ZERO) : ConfigBlock

    class Storage(var verbosity: Verbosity = Verbosity.ZERO,
                  var journal: Journal = Journal()) : ConfigBlock

    class Journal(var verbosity: Verbosity = Verbosity.ZERO) : ConfigBlock

    class Write(var verbosity: Verbosity = Verbosity.ZERO) : ConfigBlock
}
