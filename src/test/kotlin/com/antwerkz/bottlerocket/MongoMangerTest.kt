package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.clusters.SingleNode
import org.testng.Assert.fail
import org.testng.annotations.Test

class MongoMangerTest {
    @Test
    fun addUsers() {
        val node = SingleNode
            .builder()
            .build()

        node.start()

        node.addUser("test", "rocket-user", "i'm a dummy", listOf())

        try {
            node.addUser("test", "rocket-user", "i'm a dummy", listOf())
        } catch (e: Exception) {
            fail("Adding a duplicate user should not have failed.", e)
        } finally {
            node.shutdown()
        }
    }
}