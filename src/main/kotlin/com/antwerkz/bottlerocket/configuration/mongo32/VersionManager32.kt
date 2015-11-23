package com.antwerkz.bottlerocket.configuration.mongo32

import com.antwerkz.bottlerocket.configuration.mongo30.VersionManager30
import com.github.zafarkhaja.semver.Version
import com.antwerkz.bottlerocket.configuration.Configuration as BaseConfiguration

class VersionManager32(version: Version) : VersionManager30(version) {
}
