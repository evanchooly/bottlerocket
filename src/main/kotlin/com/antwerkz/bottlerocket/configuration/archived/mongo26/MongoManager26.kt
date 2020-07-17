package com.antwerkz.bottlerocket.configuration.archived.mongo26

import com.antwerkz.bottlerocket.MongoManager
import com.github.zafarkhaja.semver.Version

abstract class MongoManager26(version: Version): MongoManager(version)