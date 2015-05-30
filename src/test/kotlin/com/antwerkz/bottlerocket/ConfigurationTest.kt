package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.configuration.ConfigBlock
import com.antwerkz.bottlerocket.configuration.Configuration
import com.antwerkz.bottlerocket.configuration.Destination
import com.antwerkz.bottlerocket.configuration.Verbosity
import org.testng.Assert
import org.testng.annotations.Test

public class ConfigurationTest {
    Test public fun testYaml() {
        val configuration = Configuration()
        configuration.systemLog.destination = Destination.SYSLOG
        configuration.systemLog.component.accessControl = Verbosity.FIVE
        Assert.assertEquals(configuration.toYaml(),
              "systemLog:\n" +
              "    component:\n" +
              "      accessControl: 5\n" +
              "    destination: SYSLOG")
    }

    Test public fun printAll() {
        ConfigBlock.OMIT_DEFAULTED = false
        println("Configuration() = ${Configuration().toYaml()}")
    }
}