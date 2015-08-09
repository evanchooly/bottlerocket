package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.configuration.mongo30.Configuration
import com.github.zafarkhaja.semver.Version
import java.io.File

interface VersionManager {
    val version: Version
    fun writeConfig(configFile: File, config: Configuration)
}

abstract class BaseVersionManager(override val version: Version): VersionManager {
    companion object {
        fun of(version: Version): VersionManager {
            return when(version.getNormalVersion()) {
                "3.0" -> VersionManager30(version);
                "2.6" -> throw RuntimeException(version.toString())
                else -> VersionManager30(version);
            }
        }
    }

    override fun writeConfig(configFile: File, config: Configuration) {
        configFile.writeText(config.toYaml())
    }
}

class VersionManager30(version: Version): BaseVersionManager(version) {
}