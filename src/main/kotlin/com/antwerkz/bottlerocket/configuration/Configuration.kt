package com.antwerkz.bottlerocket.configuration

annotation class Since(val version: String)

class Configuration(
      var systemLog: SystemLog = SystemLog(),
      var processManagement: ProcessManagement = ProcessManagement(),
      var net: Net = Net(),
      var security: Security = Security(),
      var operationProfiling: OperationProfiling = OperationProfiling(),
      var storage: Storage = Storage(),
      var replication: Replication = Replication(),
      var sharding: Sharding = Sharding(),
      var auditLog: AuditLog = AuditLog(),
      var snmp: Snmp = Snmp()
) : ConfigBlock {
    override fun toYaml(): String {
        return Configuration::class.properties
              .map { (it.get(this) as ConfigBlock).toYaml() }
              .filter { it != "" }
              .toList()
              .join("\n")
    }

    fun systemLog(init: SystemLog.() -> Unit) {
        systemLog = initConfigBlock(SystemLog(), init)
    }

    fun processManagement(init: ProcessManagement.() -> Unit) {
        processManagement = initConfigBlock(ProcessManagement(), init)
    }

    fun net(init: Net.() -> Unit) {
        net = initConfigBlock(Net(), init)
    }

    fun security(init: Security.() -> Unit) {
        security = initConfigBlock(Security(), init)
    }

    fun operationProfiling(init: OperationProfiling.() -> Unit) {
        operationProfiling = initConfigBlock(OperationProfiling(), init)
    }

    fun storage(init: Storage.() -> Unit) {
        storage = initConfigBlock(Storage(), init)
    }

    fun replication(init: Replication.() -> Unit) {
        replication = initConfigBlock(Replication(), init)
    }

    fun sharding(init: Sharding.() -> Unit) {
        sharding = initConfigBlock(Sharding(), init)
    }

    fun auditLog(init: AuditLog.() -> Unit) {
        auditLog = initConfigBlock(AuditLog(), init)
    }

    fun snmp(init: Snmp.() -> Unit) {
        snmp = initConfigBlock(Snmp(), init)
    }
}

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

class ProcessManagement(
      var pidFilePath: String? = null,
      var fork: Boolean = false,
      var windowsService: WindowsService = WindowsService()
) : ConfigBlock {
    fun windowsService(init: WindowsService.() -> Unit) {
        windowsService = initConfigBlock(WindowsService(), init)
    }
}

class Component(
      Since("3.0") var accessControl: LogComponent.AccessControl = LogComponent.AccessControl(),
      Since("3.0") var command: LogComponent.Command = LogComponent.Command(),
      Since("3.0") var control: LogComponent.Control = LogComponent.Control(),
      Since("3.0") var geo: LogComponent.Geo = LogComponent.Geo(),
      Since("3.0") var index: LogComponent.Index = LogComponent.Index(),
      Since("3.0") var network: LogComponent.Network = LogComponent.Network(),
      Since("3.0") var query: LogComponent.Query = LogComponent.Query(),
      Since("3.0") var replication: LogComponent.Replication = LogComponent.Replication(),
      Since("3.0") var sharding: LogComponent.Sharding = LogComponent.Sharding(),
      Since("3.0") var storage: LogComponent.Storage = LogComponent.Storage(),
      Since("3.0") var storageJournal: LogComponent.StorageJournal = LogComponent.StorageJournal(),
      Since("3.0") var write: LogComponent.Write = LogComponent.Write()
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

    fun storageJournal(init: LogComponent.StorageJournal.() -> Unit) {
        storageJournal = initConfigBlock(LogComponent.StorageJournal(), init)
    }

    fun write(init: LogComponent.Write.() -> Unit) {
        write = initConfigBlock(LogComponent.Write(), init)
    }
}

class Net(
      var port: Int = 27017,
      var bindIp: String = "127.0.0.1",
      var maxIncomingConnections: Int = 65536,
      var wireObjectCheck: Boolean = true,
      var unixDomainSocket: UnixDomainSocket = UnixDomainSocket(),
      var ipv6: Boolean = false,
      var http: Http = Http(),
      var ssl: Ssl = Ssl(),
      var security: Security = Security()
) : ConfigBlock {
    fun unixDomainSocket(init: UnixDomainSocket.() -> Unit) {
        unixDomainSocket = initConfigBlock(UnixDomainSocket(), init)
    }

    fun http(init: Http.() -> Unit) {
        http = initConfigBlock(Http(), init)
    }

    fun ssl(init: Ssl.() -> Unit) {
        ssl = initConfigBlock(Ssl(), init)
    }

    fun security(init: Security.() -> Unit) {
        security = initConfigBlock(Security(), init)
    }
}

class UnixDomainSocket(
      var enabled: Boolean = true,
      var pathPrefix: String = "/tmp"
) : ConfigBlock

class Http(
      public Since("2.6") var enabled: Boolean = false,
      var JSONPEnabled: Boolean = false,
      var RESTInterfaceEnabled: Boolean = false
) : ConfigBlock

class Ssl(
      deprecated("Deprecated since version 2.6.")
      var sslOnNormalPorts: Boolean = false,
      Since("2.6") var mode: SslMode = SslMode.DISABLED,
      Since("2.2") var PEMKeyFile: String? = null,
      Since("2.2") var PEMKeyPassword: String? = null,
      Since("2.6") var clusterFile: String? = null,
      Since("2.6") var clusterPassword: String? = null,
      Since("2.4") var CAFile: String? = null,
      Since("2.4") var CRLFile: String? = null,
      Since("2.4") var allowConnectionsWithoutCertificates: Boolean = false,
      Since("2.6") var allowInvalidCertificates: Boolean = false,
      Since("3.0") var allowInvalidHostnames: Boolean = false,
      Since("2.4") var FIPSMode: Boolean = false,
      var setParameter: Map<String, String> = mapOf()  // TODO
) : ConfigBlock

class Security(
      var keyFile: String? = null,
      Since("2.6") var clusterAuthMode: ClusterAuthMode = ClusterAuthMode.KEY_FILE,
      var authorization: State = State.DISABLED,
      var sasl: Sasl = Sasl(),
      var javascriptEnabled: Boolean = true
) : ConfigBlock {
    fun sasl(init: Sasl.() -> Unit) {
        sasl = initConfigBlock(Sasl(), init)
    }
}

class Sasl(
      var hostName: String? = null,
      var serviceName: String = "mongodb",
      var saslauthdSocketPath: String? = null
) : ConfigBlock

class OperationProfiling(
      var slowOpThresholdMs: Int = 100,
      var mode: ProfilingMode = ProfilingMode.OFF
) : ConfigBlock

class Storage(
      var dbPath: String = "/data/db",
      var indexBuildRetry: Boolean = true,
      var repairPath: String = dbPath + "_tmp",
      var journal: Journal = Journal(),
      var directoryPerDB: Boolean = false,
      var syncPeriodSecs: Int = 60,
      Since("3.0") var engine: String = "mmapv1",
      var mmapv1: Mmapv1 = Mmapv1(),
      Since("3.0.0") var wiredTiger: WiredTiger = WiredTiger()
) : ConfigBlock {

    fun journal(init: Journal.() -> Unit) {
        journal = initConfigBlock(Journal(), init)
    }

    fun mmapv1(init: Mmapv1.() -> Unit) {
        mmapv1 = initConfigBlock(Mmapv1(), init)
    }

    fun wiredTiger(init: WiredTiger.() -> Unit) {
        wiredTiger = initConfigBlock(WiredTiger(), init)
    }
}

class Mmapv1(
      var preallocDataFiles: Boolean = true,
      var nsSize: Int = 16,
      var quota: Mmapv1.Quota = Mmapv1.Quota(),
      var smallFiles: Boolean = false,
      var journal: Mmapv1.Journal = Mmapv1.Journal()
) : ConfigBlock {

    fun quota(init: Mmapv1.Quota.() -> Unit) {
        quota = initConfigBlock(Mmapv1.Quota(), init)
    }

    fun journal(init: Mmapv1.Journal.() -> Unit) {
        journal = initConfigBlock(Mmapv1.Journal(), init)
    }

    class Quota(
          var enforced: Boolean = false,
          var maxFilesPerDB: Int = 8
    ) : ConfigBlock

    class Journal(
          var debugFlags: Int = 0,
          var commitIntervalMs: Int = 100
    ) : ConfigBlock

}

class Journal(
      var enabled: Boolean = true
) : ConfigBlock


class WiredTiger(
      var engineConfig: EngineConfig = EngineConfig()
) : ConfigBlock {

    fun engineConfig(init: EngineConfig.() -> Unit) {
        engineConfig = initConfigBlock(EngineConfig(), init)
    }
}

class EngineConfig(
      Since("3.0.0") var cacheSizeGB: Int? = null,
      Since("3.0.0") var statisticsLogDelaySecs: Int = 0,
      Since("3.0.0") var journalCompressor: Compressor = Compressor.SNAPPY,
      Since("3.0.0") var directoryForIndexes: Boolean = false,
      Since("3.0.0") var collectionConfig: CollectionConfig = CollectionConfig(),
      Since("3.0.0") var indexConfig: IndexConfig = IndexConfig()

) : ConfigBlock {

    fun collectionConfig(init: CollectionConfig.() -> Unit) {
        collectionConfig = initConfigBlock(CollectionConfig(), init)
    }

    fun indexConfig(init: IndexConfig.() -> Unit) {
        indexConfig = initConfigBlock(IndexConfig(), init)
    }
}

class CollectionConfig(
      Since("3.0.0") var blockCompressor: Compressor = Compressor.SNAPPY

) : ConfigBlock

class IndexConfig(
      Since("3.0.0") var prefixCompression: Boolean = true

) : ConfigBlock

class Replication(
      var oplogSizeMB: Int? = null,
      var replSetName: String? = null,
      Since("2.2") var secondaryIndexPrefetch: IndexPrefetch = IndexPrefetch.ALL,
      var localPingThresholdMs: Int = 15

) : ConfigBlock

class Sharding(
      var clusterRole: ClusterRole? = null,
      Since("2.4") var archiveMovedChunks: Boolean = true,
      var autoSplit: Boolean = true,
      var configDB: String? = null,
      var chunkSize: Int = 64
) : ConfigBlock

class AuditLog(
      Since("2.6") var destination: LogDestination? = null,
      Since("2.6") var format: LogFormat? = null,
      Since("2.6") var path: String? = null,
      Since("2.6") var filter: String? = null
) : ConfigBlock

class Snmp(
      var subAgent: Boolean = true,
      var master: Boolean = false
) : ConfigBlock

class WindowsService(
      var serviceName: String = "MongoDB",
      var displayName: String = "MongoDB",
      var description: String = "MongoDB Server",
      var serviceUser: String? = null,
      var servicePassword: String? = null
) : ConfigBlock


fun configuration(init: Configuration.() -> Unit): Configuration {
    val configuration = Configuration()
    configuration.init()
    return configuration
}
