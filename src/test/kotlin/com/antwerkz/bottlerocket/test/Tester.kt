package com.antwerkz.bottlerocket.test

import com.antwerkz.bottlerocket.BottleRocket
import com.antwerkz.bottlerocket.TestBase
import com.github.zafarkhaja.semver.Version
import org.testng.Assert.assertNotNull
import org.testng.annotations.Test

class Tester: TestBase() {
    override fun version(): Version? {
        return BottleRocket.DEFAULT_VERSION
    }

    @Test
    fun connect() {
        val list = mongoClient.listDatabaseNames().toList()
        assertNotNull(list)
    }
}