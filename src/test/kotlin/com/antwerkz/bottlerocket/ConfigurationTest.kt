package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.configuration.*
import org.testng.Assert
import org.testng.annotations.Test

public class ConfigurationTest {
    Test public fun testYaml() {
        val configuration = Configuration(
            systemLog = SystemLog(
                  destination = Destination.SYSLOG,
                  component = Component(
                        accessControl = LogComponent.AccessControl(verbosity = Verbosity.FIVE)
                  )
            )
        )
        val target =
              "systemLog:\n" +
              "  component:\n" +
              "    accessControl:\n" +
              "      verbosity: 5\n" +
              "  destination: syslog"
        Assert.assertEquals(configuration.toYaml(), target)
    }

    Test public fun complexExample() {
        val configuration = Configuration(
              storage = Storage(dbPath = "/var/lib/mongodb"),
              systemLog = SystemLog(
                    destination = Destination.FILE,
                    path = "/var/log/mongodb/mongod.log",
                    logAppend = true,
                    logRotate = RotateBehavior.RENAME,
                    component = Component(
                          accessControl = LogComponent.AccessControl(verbosity = Verbosity.TWO)
                    )
              ),
              processManagement = ProcessManagement(
                    fork = true
              )
        )
        val target =
              "processManagement:\n" +
                    "  fork: true\n" +
                    "storage:\n" +
                    "  dbPath: /var/lib/mongodb\n" +
                    "  repairPath: /var/lib/mongodb_tmp\n" +
                    "systemLog:\n" +
                    "  component:\n" +
                    "    accessControl:\n" +
                    "      verbosity: 2\n" +
                    "  destination: file\n" +
                    "  logAppend: true\n" +
                    "  path: /var/log/mongodb/mongod.log" +
                    ""
        //              "setParameter:\n" +
//              "   enableLocalhostAuthBypass: false\n" +
        Assert.assertEquals(configuration.toYaml(), target);
    }

    public fun printAll() {
        ConfigBlock.OMIT_DEFAULTED = false
        println("Configuration() = ${Configuration().toYaml()}")
    }
}