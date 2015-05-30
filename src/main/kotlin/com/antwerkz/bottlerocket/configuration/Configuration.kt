package com.antwerkz.bottlerocket.configuration

annotation class Since(val version: String)

class Configuration(
      public var systemLog: SystemLog = SystemLog(),
      public var processManagement: ProcessManagement = ProcessManagement(),
      public var net: Net = Net(),
      public var security: Security = Security(),
      public var operationProfiling: OperationProfiling = OperationProfiling(),
      public var storage: Storage = Storage(),
      public var replication: Replication = Replication(),
      public var sharding: Sharding = Sharding(),
      public var auditLog: AuditLog = AuditLog(),
      public var snmp: Snmp = Snmp()
) : ConfigBlock {
      override fun toYaml(): String {
            return Configuration::class.properties
                  .map { (it.get(this) as ConfigBlock).toYaml() }
                  .filter { it != "" }
                  .toList()
                  .join("\n")
      }
}

class SystemLog(
      Since("3.0") public var verbosity: Verbosity = Verbosity.ZERO,
      public var component: Component = Component(),
      public var quiet: Boolean = false,
      public var traceAllExceptions: Boolean = false,
      public var syslogFacility: String = "user",
      public var path: String? = null,
      public var logAppend: Boolean = false,
      Since("3.0") public var logRotate: RotateBehavior = RotateBehavior.RENAME,
      public var destination: Destination = Destination.STANDARD_OUT,
      public var timeStampFormat: TimestampFormat = TimestampFormat.ISO8601_LOCAL) : ConfigBlock

class ProcessManagement(
      public var pidFilePath: String? = null,
      public var fork: Boolean = false,
      public var windowsService: WindowsService = WindowsService()
) : ConfigBlock

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
) : ConfigBlock

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
) : ConfigBlock

class UnixDomainSocket(
      var enabled: Boolean = true,
      var pathPrefix: String = "/tmp"
) : ConfigBlock

class Http(
      Since("2.6") var enabled: Boolean = false,
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
) : ConfigBlock

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
) : ConfigBlock

class Mmapv1(
      var preallocDataFiles: Boolean = true,
      var nsSize: Int = 16,
      var quota: Mmapv1.Quota = Mmapv1.Quota(),
      var smallFiles: Boolean = false,
      var journal: Mmapv1.Journal = Mmapv1.Journal()
) : ConfigBlock {

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
      public var enabled: Boolean = true
) : ConfigBlock


class WiredTiger(
      var engineConfig: EngineConfig = EngineConfig()
) : ConfigBlock

class EngineConfig(
      Since("3.0.0") var cacheSizeGB: Int? = null,
      Since("3.0.0") var statisticsLogDelaySecs: Int = 0,
      Since("3.0.0") var journalCompressor: Compressor = Compressor.SNAPPY,
      Since("3.0.0") var directoryForIndexes: Boolean = false,
      Since("3.0.0") var collectionConfig: CollectionConfig = CollectionConfig(),
      Since("3.0.0") var indexConfig: IndexConfig = IndexConfig()

) : ConfigBlock

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
