package com.antwerkz.bottlerocket.configuration

import com.antwerkz.bottlerocket.configuration.types.Destination
import com.antwerkz.bottlerocket.configuration.types.RotateBehavior
import com.antwerkz.bottlerocket.configuration.types.State
import com.antwerkz.bottlerocket.configuration.types.Verbosity
import org.testng.Assert
import org.testng.annotations.Test

class ConfigurationTest {
    companion object {
        @JvmStatic
        val complexConfig: String =
                """net:
  bindIp: 127.0.0.1
  port: 27017
processManagement:
  fork: true
replication:
  oplogSizeMB: 10
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
                """net:
  bindIp: 127.0.0.1
  port: 27017
replication:
  oplogSizeMB: 10
systemLog:
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
  port: 27017
processManagement:
  fork: true
replication:
  oplogSizeMB: 10
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
                """net:
  bindIp: 127.0.0.1
  port: 27017
replication:
  oplogSizeMB: 10
systemLog:
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
        Assert.assertTrue(-1 == yaml.indexOf(path), "Found '${path}' in \n${yaml}")
        Assert.assertTrue(-1 == yaml.indexOf("oplogSizeMB"), "Found 'oplogSizeMB' in \n${yaml}")
    }

    @Test
    fun mergeConfig() {
        val config = configuration {
            storage {
                dbPath = "/var/lib/mongo/noodle"
            }
            net {
                port = 12345
            }
        }
        val update = configuration {
            processManagement {
                operationProfiling {
                    slowOpThresholdMs = 50
                }
            }
            security {
                authorization = State.ENABLED
            }
        }

        Assert.assertNull(config.security.authorization)
        config.merge(update)
        Assert.assertEquals(config.security.authorization, State.ENABLED)
        Assert.assertEquals(config.operationProfiling.slowOpThresholdMs, 50)
        Assert.assertNotEquals(update.storage.dbPath, "/var/lib/mongo/noodle")
    }
}