package com.antwerkz.bottlerocket.configuration

import com.antwerkz.bottlerocket.configuration.types.Destination
import com.antwerkz.bottlerocket.configuration.types.RotateBehavior
import com.antwerkz.bottlerocket.configuration.types.State
import com.antwerkz.bottlerocket.configuration.types.State.ENABLED
import com.antwerkz.bottlerocket.configuration.types.Verbosity
import org.testng.Assert
import org.testng.annotations.Test

class ConfigurationTest {
    companion object {
        @JvmStatic
        val complexConfig: String =
            """processManagement:
  fork: true
storage:
  dbPath: /var/lib/mongodb
  repairPath: /var/lib/mongodb_tmp
systemLog:
  component:
    accessControl:
      verbosity: 2
  destination: file
  logAppend: true
  logRotate: rename
  path: /var/log/mongodb/mongod.log
"""
        //              "setParameter:\n" +
        //              "   enableLocalhostAuthBypass: false\n" +
    }

    @Test
    fun testYaml() {
        val configuration =
            configuration {
                systemLog {
                    destination = Destination.SYSLOG
                    component {
                        systemLog { }
                        accessControl {
                            verbosity = Verbosity.FIVE
                        }
                    }
                }
            }
        val target =
            """systemLog:
  component:
    accessControl:
      verbosity: 5
  destination: syslog
"""

        Assert.assertEquals(configuration.toYaml(), target)
    }

    @Test
    fun complexExample() {
        val configuration = configuration {
            storage {
                dbPath = "/var/lib/mongodb"
            }
            systemLog {
                destination = Destination.FILE
                path = "/var/log/mongodb/mongod.log"
                logAppend = true
                component {
                    net {}
                    storage { }
                    accessControl {
                        verbosity = Verbosity.TWO
                    }
                }
            }
            processManagement {
                fork = true
            }
        }
        val target =
            """net:
  bindIp: 127.0.0.1
processManagement:
  fork: true
storage:
  dbPath: /var/lib/mongodb
systemLog:
  component:
    accessControl:
      verbosity: 2
  destination: file
  logAppend: true
  path: /var/log/mongodb/mongod.log
"""
        Assert.assertEquals(configuration.toYaml(), target)
    }

    @Test
    fun testBuilder() {
        val config = configuration {
            systemLog {
                destination = Destination.SYSLOG
                component {
                    accessControl {
                        verbosity = Verbosity.FIVE
                    }
                }
            }
        }
        Assert.assertEquals(config.toYaml(),
            """systemLog:
  component:
    accessControl:
      verbosity: 5
  destination: syslog
""")
    }

    @Test
    fun testComplexBuilder() {
        val configuration = configuration {
            storage {
                dbPath = "/var/lib/mongodb"
                repairPath = "/var/lib/mongodb_tmp"
            }
            systemLog {
                destination = Destination.FILE
                path = "/var/log/mongodb/mongod.log"
                logAppend = true
                logRotate = RotateBehavior.RENAME
                component {
                    accessControl {
                        verbosity = Verbosity.TWO
                    }
                }
            }
            processManagement {
                fork = true
            }
        }

        Assert.assertEquals(configuration.toYaml(), complexConfig)
    }

    @Test
    fun mongosConfig() {
        val path = "/var/lib/mongo/data"
        val configuration = configuration {
            replication {
                oplogSizeMB = 100
            }
            storage {
                dbPath = path
            }
        }
        val yaml = configuration.toYaml(mode = ConfigMode.MONGOS)
        Assert.assertTrue(-1 == yaml.indexOf(path), "Found '$path' in \n$yaml")
        Assert.assertTrue(-1 == yaml.indexOf("oplogSizeMB"), "Found 'oplogSizeMB' in \n$yaml")
    }

    @Test
    fun smallMerges() {
        var config = configuration {
            auditLog {
                filter = "filter value"
            }
        }
        var update: Configuration = config.update {
            auditLog {
                filter = "updated"
            }
        }

        Assert.assertEquals(config.auditLog?.filter, "filter value")
        Assert.assertEquals(update.auditLog?.filter, "updated")

        config = configuration {
            net {
                port = 12345
            }
        }

        update = config.update {
            net {
                port = 49152
            }
        }

        Assert.assertEquals(config.net?.port, 12345)
        Assert.assertEquals(update.net?.port, 49152)
    }

    @Test
    fun mergeConfig() {
        val config = configuration {
            auditLog {
                filter = "filter value"
            }
            storage {
                dbPath = "/var/lib/mongo/noodle"
            }
            net {
                port = 12345
            }
        }

        Assert.assertEquals(config.auditLog?.filter, "filter value")
        Assert.assertEquals(config.storage?.dbPath, "/var/lib/mongo/noodle")
        Assert.assertEquals(config.net?.port, 12345)
        Assert.assertNull(config.operationProfiling?.slowOpThresholdMs)
        Assert.assertNull(config.security?.authorization)
        val updated: Configuration = config.update {
            net {
                port = 49152
            }
            operationProfiling {
                slowOpThresholdMs = 50
            }
            security {
                authorization = ENABLED
            }
        }

        Assert.assertEquals(config.auditLog?.filter, "filter value")
        Assert.assertEquals(config.storage?.dbPath, "/var/lib/mongo/noodle")
        Assert.assertEquals(config.net?.port, 12345)
        Assert.assertNull(config.operationProfiling?.slowOpThresholdMs)
        Assert.assertNull(config.security?.authorization)

        Assert.assertEquals(updated.auditLog?.filter, "filter value")
        Assert.assertEquals(updated.storage?.dbPath, "/var/lib/mongo/noodle")
        Assert.assertEquals(updated.net?.port, 49152)
        Assert.assertEquals(updated.security?.authorization, State.ENABLED)
        Assert.assertEquals(updated.operationProfiling?.slowOpThresholdMs, 50)
    }
}