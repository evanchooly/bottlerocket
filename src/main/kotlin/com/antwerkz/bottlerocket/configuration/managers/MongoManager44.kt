package com.antwerkz.bottlerocket.configuration.managers

import com.antwerkz.bottlerocket.MongoManager
import com.github.zafarkhaja.semver.Version

class MongoManager44(version: Version) : MongoManager(version,
    linuxBaseUrl = "https://fastdl.mongodb.org/linux/mongodb-linux-x86_64-${linux()}-",
    macBaseUrl = "https://fastdl.mongodb.org/osx/mongodb-macos-x86_64-",
    windowsBaseUrl = "https://fastdl.mongodb.org/windows/mongodb-windows-x86_64-"
)
