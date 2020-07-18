package com.antwerkz.bottlerocket.configuration.mongo36

import com.antwerkz.bottlerocket.MongoManager
import com.github.zafarkhaja.semver.Version

class MongoManager36(version: Version) : MongoManager(version,
        linuxBaseUrl = "https://fastdl.mongodb.org/linux/mongodb-linux-x86_64-ubuntu1604-",
        macBaseUrl = "https://fastdl.mongodb.org/osx/mongodb-osx-ssl-x86_64-",
        windowsBaseUrl = "https://fastdl.mongodb.org/win32/mongodb-win32-x86_64-2008plus-ssl-"
)