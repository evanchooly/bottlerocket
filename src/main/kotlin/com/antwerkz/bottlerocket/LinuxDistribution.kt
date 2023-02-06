package com.antwerkz.bottlerocket

import org.slf4j.LoggerFactory
import java.io.File
import java.util.Properties

@Suppress("unused")
sealed class LinuxDistribution(private val meta: Properties = Properties()) {
    companion object {
        private val LOG = LoggerFactory.getLogger(LinuxDistribution::class.java)
        internal fun parse(osRelease: File): LinuxDistribution {
            return if (osRelease.exists()) {
                val props = Properties()
                osRelease.inputStream().use {
                    props.load(it)
                }
                val name = props.getProperty("NAME").replace("\"", "")
                when (name) {
                    "Ubuntu" -> Ubuntu(props)
                    "Fedora" -> Fedora(props)
                    "Fedora Linux" -> Fedora(props)
                    else -> throw UnsupportedOperationException("Unknown distribution:  $name")
                }
            } else {
                LOG.warn("No /etc/os-release file found.  Assuming Ubuntu.")
                val props = Properties()
                props["VERSION_ID"] = "20.04"
                Ubuntu(props)
            }
        }
    }

    open fun name(): String = meta.getProperty("ID")
    open fun version(): String = meta.getProperty("VERSION_ID").replace("\"", "")
    abstract fun mongoVersion(): String
    override fun toString(): String {
        return "${name()} ${version()} [MongoDB qualifier: ${mongoVersion()}]"
    }

    class Fedora(props: Properties) : LinuxDistribution(props) {
        override fun mongoVersion(): String = "rhel80"
    }

    class Ubuntu(props: Properties) : LinuxDistribution(props) {
        override fun mongoVersion(): String = "ubuntu" + when (version()) {
            "18.04", "20.04" -> version().replace(".", "")
            else -> "1804"
        }
    }

    internal class TestDistro(val name: String, val version: String, val mongoVersion: String, val id: String = name.lowercase()) 
        : LinuxDistribution() {
        override fun name() = name
        override fun mongoVersion() = mongoVersion
        override fun version() = version
    }
}
