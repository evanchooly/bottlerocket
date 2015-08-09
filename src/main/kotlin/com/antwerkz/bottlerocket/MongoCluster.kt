package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.configuration.mongo30.Configuration
import com.antwerkz.bottlerocket.configuration.mongo30.configuration
import com.antwerkz.bottlerocket.configuration.types.ClusterAuthMode
import com.antwerkz.bottlerocket.configuration.types.Compressor
import com.antwerkz.bottlerocket.configuration.types.Destination
import com.antwerkz.bottlerocket.configuration.types.IndexPrefetch
import com.antwerkz.bottlerocket.configuration.types.ProfilingMode
import com.antwerkz.bottlerocket.configuration.types.RotateBehavior
import com.antwerkz.bottlerocket.configuration.types.SslMode
import com.antwerkz.bottlerocket.configuration.types.State
import com.antwerkz.bottlerocket.configuration.types.TimestampFormat
import com.antwerkz.bottlerocket.configuration.types.Verbosity
import com.mongodb.MongoClient
import com.mongodb.MongoClientOptions
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import org.slf4j.LoggerFactory
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.PosixFilePermission
import java.util.EnumSet

val TEMP_DIR = if (File("/tmp").exists()) "/tmp" else System.getProperty("java.io.tmpdir")

val DEFAULT_NAME = "rocket"
val DEFAULT_PORT = 30000
val DEFAULT_VERSION = "3.0.5"
val DEFAULT_BASE_DIR = File("${TEMP_DIR}/${DEFAULT_NAME}")

public abstract class MongoCluster(public val name: String = DEFAULT_NAME,
                                   public val port: Int = DEFAULT_PORT,
                                   val version: String = DEFAULT_VERSION,
                                   public val baseDir: File = DEFAULT_BASE_DIR) {

    val mongoManager: MongoManager = MongoManager.of(version)
    private var client: MongoClient? = null;
    var adminAdded: Boolean = false
    val keyFile: String = File(baseDir, "rocket.key").getAbsolutePath()
    val pemFile: String = File(baseDir, "rocket.pem").getAbsolutePath()

    companion object {
        val DEFAULT_CONFIG: Configuration =
              configuration {
                  net {
                      port = 27017
                      bindIp = "127.0.0.1"
                      maxIncomingConnections = 65536
                      wireObjectCheck = true
                      ipv6 = false
                      http {
                          enabled = false
                          JSONPEnabled = false
                          RESTInterfaceEnabled = false
                      }
                      ssl {
                          @SuppressWarnings("deprecated")
                          sslOnNormalPorts: Boolean = false
                          mode: SslMode = SslMode.DISABLED
                          allowConnectionsWithoutCertificates: Boolean = false
                          allowInvalidCertificates: Boolean = false
                          allowInvalidHostnames: Boolean = false
                          FIPSMode: Boolean = false
                      }
                      unixDomainSocket {
                          enabled = true
                          pathPrefix = "/tmp"
                          filePermissions = 700
                      }
                  }
                  operationProfiling {
                      slowOpThresholdMs = 100
                      mode = ProfilingMode.OFF

                  }
                  processManagement {
                      fork = false
                      windowsService {
                          serviceName = "MongoDB"
                          displayName = "MongoDB"
                          description = "MongoDB Server"
                      }

                  }
                  replication {
                      secondaryIndexPrefetch = IndexPrefetch.ALL
                      localPingThresholdMs = 15
                  }
                  security {
                      clusterAuthMode = ClusterAuthMode.KEY_FILE
                      authorization = State.DISABLED
                      javascriptEnabled = true
                      sasl {
                          serviceName = "mongodb"
                      }
                  }
                  sharding {
                      archiveMovedChunks = true
                      autoSplit = true
                      chunkSize = 64
                  }
                  snmp {
                      subagent = true
                      master = false
                  }
                  storage {
                      dbPath = "/data/db"
                      indexBuildRetry = true
                      repairPath = dbPath + "_tmp"
                      directoryPerDB = false
                      syncPeriodSecs = 60
                      engine: String = "mmapv1"
                      journal {
                          enabled = true
                      }
                      mmapv1 {
                          preallocDataFiles = true
                          nsSize = 16
                          smallFiles = false
                          journal {
                              debugFlags = 0
                              commitIntervalMs = 100
                          }
                          quota {
                              enforced = false
                              maxFilesPerDB = 8
                          }
                      }
                      wiredTiger {
                          collectionConfig {
                              blockCompressor = Compressor.SNAPPY
                          }
                          engineConfig {
                              statisticsLogDelaySecs = 0
                              journalCompressor = Compressor.SNAPPY
                              directoryForIndexes = false
                          }
                          indexConfig {
                              prefixCompression = true
                          }
                      }
                  }
                  systemLog {
                      verbosity = Verbosity.ZERO
                      quiet = false
                      traceAllExceptions = false
                      syslogFacility = "user"
                      logAppend = false
                      logRotate = RotateBehavior.RENAME
                      destination = Destination.STANDARD_OUT
                      timeStampFormat = TimestampFormat.ISO8601_LOCAL
                      component {
                          accessControl { verbosity = Verbosity.ZERO }
                          command { verbosity = Verbosity.ZERO }
                          control { verbosity = Verbosity.ZERO }
                          geo { verbosity = Verbosity.ZERO }
                          index { verbosity = Verbosity.ZERO }
                          network { verbosity = Verbosity.ZERO }
                          query { verbosity = Verbosity.ZERO }
                          replication { verbosity = Verbosity.ZERO }
                          sharding { verbosity = Verbosity.ZERO }
                          storage {
                              verbosity = Verbosity.ZERO
                              journal { verbosity = Verbosity.ZERO }
                          }
                          write { verbosity = Verbosity.ZERO }
                      }
                  }
              }
    }

    init {
        baseDir.mkdirs()
    }

    abstract fun start();

    abstract fun isAuthEnabled(): Boolean;

    abstract fun isStarted(): Boolean

    abstract fun getServerAddressList(): List<ServerAddress>

    open fun shutdown() {
        client?.close()
        client = null;
    }

    open fun enableAuth() {
        generateKeyFile()
        generatePemFile()
    }

    open fun clean() {
        shutdown();
        baseDir.deleteTree()
    }

    fun getClient(): MongoClient {
        if (client == null) {
            val builder = MongoClientOptions.builder()
                  .connectTimeout(3000)
            client = MongoClient(getServerAddressList(), getCredentials(), builder.build())
        }

        return client!!;
    }

    fun getCredentials(): List<MongoCredential> {
        var credentials = if (isAuthEnabled()) {
            arrayListOf(MongoCredential.createCredential(MongoExecutable.SUPER_USER, "admin",
                  MongoExecutable.SUPER_USER_PASSWORD.toCharArray()))
        } else {
            listOf<MongoCredential>()
        }
        return credentials
    }

    fun generateKeyFile() {
        val key = File(keyFile)
        if (!key.exists()) {
            key.getParentFile().mkdirs()
            val stream = FileOutputStream(key)
            try {
                ProcessExecutor()
                      .command(listOf("openssl", "rand", "-base64", "741"))
                      .redirectOutput(stream)
                      .redirectError(Slf4jStream.of(LoggerFactory.getLogger(javaClass)).asInfo())
                      .execute()
            } finally {
                stream.close()
            }
        }
        Files.setPosixFilePermissions(key.toPath(), EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE))
    }

    fun generatePemFile() {
        val pem = File(pemFile)
        val key = File(baseDir, "rocket-pem.key")
        val crt = File(baseDir, "rocket-pem.crt")
        if (!pem.exists()) {
            var openssl = "openssl req -batch -newkey rsa:2048 -new -x509 -days 365 -nodes -out ${crt.getAbsolutePath()} -keyout ${key
                  .getAbsolutePath()}";
            var cat = "cat ${key.getAbsolutePath()} ${crt.getAbsolutePath()}"
            pem.getParentFile().mkdirs()
            val stream = FileOutputStream(pem.getAbsolutePath())
            ProcessExecutor()
                  .directory(baseDir)
                  .command(openssl.splitBy(" "))
                  .redirectOutputAsDebug()
                  .redirectError(Slf4jStream.of(LoggerFactory.getLogger(javaClass)).asError())
                  .execute()
            ProcessExecutor()
                  .directory(baseDir)
                  .command(cat.splitBy(" "))
                  .redirectOutput(stream)
                  .redirectError(Slf4jStream.of(LoggerFactory.getLogger(javaClass)).asError())
                  .execute()
        }
        Files.setPosixFilePermissions(pem.toPath(), EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE))
        Files.setPosixFilePermissions(key.toPath(), EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE))
        Files.setPosixFilePermissions(crt.toPath(), EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE))
    }

    abstract fun updateConfig(update: Configuration)

    abstract fun allNodesActive()
}

fun File.deleteTree() {
    if (exists()) {
        if (isDirectory()) {
            Files.walkFileTree(toPath(), object : SimpleFileVisitor<Path>() {
                override public fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                override public fun postVisitDirectory(dir: Path, e: IOException?): FileVisitResult {
                    if (e == null) {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    } else {
                        // directory iteration failed
                        throw e;
                    }
                }
            });
        } else {
            throw RuntimeException("deleteTree() can only be called on directories:  ${this}")
        }
    }
}