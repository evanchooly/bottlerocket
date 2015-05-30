package com.antwerkz.bottlerocket.configuration

open class LogComponent(
      var verbosity: Verbosity = Verbosity.ZERO
) : ConfigBlock {

    class AccessControl(verbosity: Verbosity = Verbosity.ZERO) : LogComponent(verbosity)

    class Command(verbosity: Verbosity = Verbosity.ZERO) : LogComponent(verbosity)

    class Control(verbosity: Verbosity = Verbosity.ZERO) : LogComponent(verbosity)

    class Geo(verbosity: Verbosity = Verbosity.ZERO) : LogComponent(verbosity)

    class Index(verbosity: Verbosity = Verbosity.ZERO) : LogComponent(verbosity)

    class Network(verbosity: Verbosity = Verbosity.ZERO) : LogComponent(verbosity)

    class Query(verbosity: Verbosity = Verbosity.ZERO) : LogComponent(verbosity)

    class Replication(verbosity: Verbosity = Verbosity.ZERO) : LogComponent(verbosity)

    class Sharding(verbosity: Verbosity = Verbosity.ZERO) : LogComponent(verbosity)

    class Storage(verbosity: Verbosity = Verbosity.ZERO) : LogComponent(verbosity)

    class StorageJournal(verbosity: Verbosity = Verbosity.ZERO) : LogComponent(verbosity)

    class Write(verbosity: Verbosity = Verbosity.ZERO) : LogComponent(verbosity)

}
      