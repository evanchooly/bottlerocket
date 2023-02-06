package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.LinuxDistribution.TestDistro
import org.testng.Assert.assertEquals
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class LinuxDistributionTest {
    @Test(dataProvider = "distros")
    private fun distros(testDistro: TestDistro, path: String) {
        val distribution = LinuxDistribution.parse(File("target/test-classes/releases/$path"))
        assertEquals(distribution.name(), testDistro.id)
        assertEquals(distribution.version(), testDistro.version())
        assertEquals(distribution.mongoVersion(), testDistro.mongoVersion())
    }

    @DataProvider(name = "distros")
    fun linux(): Array<Array<Any>> {
        return arrayOf(
            arrayOf(TestDistro("Ubuntu", "20.04", "ubuntu2004"), "ubuntu2004.release"),
            arrayOf(TestDistro("Ubuntu", "18.04", "ubuntu1804"), "ubuntu1804.release"),
            arrayOf(TestDistro("Fedora", "31", "rhel80"), "fedora31.release"),
            arrayOf(TestDistro("Fedora Linux", "37", "rhel80", "fedora"), "fedora37.release")
        )
    }
}
