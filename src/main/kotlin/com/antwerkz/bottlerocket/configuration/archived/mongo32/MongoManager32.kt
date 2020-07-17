package com.antwerkz.bottlerocket.configuration.archived.mongo32

import com.antwerkz.bottlerocket.MongoManager
import com.github.zafarkhaja.semver.Version

abstract class MongoManager32(version: Version): MongoManager(version)