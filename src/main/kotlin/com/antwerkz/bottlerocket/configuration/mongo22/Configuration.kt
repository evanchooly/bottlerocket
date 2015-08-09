package com.antwerkz.bottlerocket.configuration.mongo22

import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.ConfigMode
import com.antwerkz.bottlerocket.configuration.mongo24.Profile
import com.antwerkz.bottlerocket.configuration.types.DiagnosticLog
import com.antwerkz.bottlerocket.configuration.types.IndexPrefetch
import com.github.zafarkhaja.semver.Version

/**
 * @see http://docs.mongodb.org/v2.2/reference/configuration-options/
 */
class Configuration(
      var bind_ip: String? = "127.0.0.1",
      var port: Int? = 27017,

      var auth: Boolean? = null,
      var cpu: Boolean? = null,
      var dbpath: String? = null,
      var diaglog: DiagnosticLog? = null,
      var directoryperdb: Boolean? = null,
      var fork: Boolean? = null,
      var ipv6: Boolean? = null,
      var journal: Boolean? = null,
      var journalCommitInterval: Int? = null,
      var jsonp: Boolean? = null,
      var keyFile: String? = null,
      var logappend: Boolean? = null,
      var logpath: String? = null,
      var maxConns: Int? = null,
      var noauth: Boolean? = null,
      var nohttpinterface: Boolean? = null,
      var nojournal: Boolean? = null,
      var noMoveParanoia: Boolean? = null,
      var noprealloc: Boolean? = true,
      var noscripting: Boolean? = true,
      var notablescan: Boolean? = null,
      var nounixsocket: Boolean? = null,
      var nssize: Int? = null,
      var objcheck: Boolean? = null,
      var pidfilepath: String? = null,
      var profile: Profile? = null,
      var quiet: Boolean? = null,
      var quota: Boolean? = null,
      var quotaFiles: Int? = null,
      var repair: Boolean? = null,
      var repairpath: String? = null,
      var rest: Boolean? = null,
      var slowms: Int? = null,
      var smallfiles: Boolean? = true,
      var syncdelay: Int? = null,
      var sysinfo: Boolean? = null,
      var syslog: Boolean? = null,
      var traceExceptions: Boolean? = null,
      var unixSocketPrefix: String? = null,
      var upgrade: Boolean? = null,
      var verbose: Boolean? = null,
      var v: Boolean? = null,
      var vv: Boolean? = null,
      var vvv: Boolean? = null,
      var vvvv: Boolean? = null,
      var vvvvv: Boolean? = null,

      // replica set options
      var fastsync: Boolean? = null,
      var oplogSize: Int? = null,
      var replIndexPrefetch: IndexPrefetch? = null,
      var replSet: String? = null,

      // master/slave options
      var autoresync: Boolean? = null,
      var master: Boolean? = null,
      var only: String? = null,
      var slave: Boolean? = null,
      var slavedelay: Int? = null,
      var source: String? = null,

      // sharding options
      var chunkSize: Int? = null,
      var configdb: String? = null,
      var configsvr: Boolean? = null,
      var localThreshold: Int? = null,
      var noAutoSplit: Boolean? = null,
      var shardsvr: Boolean? = null,
      var test: Boolean? = null

) : ConfigBlock {
    override fun toMap(mode: ConfigMode, includeAll: Boolean): Map<String, Any> {
        val map = super.toMap(mode, includeAll)
        return map.get("configuration") as Map<String, Any>
    }
}

fun configuration(init: Configuration.() -> Unit): Configuration {
    val configuration = Configuration()
    configuration.init()
    return configuration
}
