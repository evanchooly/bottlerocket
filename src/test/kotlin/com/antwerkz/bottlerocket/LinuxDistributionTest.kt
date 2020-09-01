package com.antwerkz.bottlerocket

import com.antwerkz.bottlerocket.LinuxDistribution.TestDistro
import org.testng.Assert.assertEquals
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class LinuxDistributionTest {
    @Test(dataProvider = "distros")
    private fun distros(testDistro: LinuxDistribution, path: File) {
        val distribution = LinuxDistribution.parse(path)
        assertEquals(distribution.name(), testDistro.name())
        assertEquals(distribution.version(), testDistro.version())
        assertEquals(distribution.mongoVersion(), testDistro.mongoVersion())
    }

    @DataProvider(name = "distros")
    fun linux(): Array<Array<Any>> {
        return arrayOf(
            arrayOf(TestDistro("ubuntu", "20.04", "ubuntu2004"), File("target/test-classes/releases/ubuntu2004.release")),
            arrayOf(TestDistro("ubuntu", "18.04", "ubuntu1804"), File("target/test-classes/releases/ubuntu1804.release")),
            arrayOf(TestDistro("ubuntu", "16.04", "ubuntu1604"), File("target/test-classes/releases/ubuntu1604.release")),
            arrayOf(TestDistro("fedora", "31", "rhel80"), File("target/test-classes/releases/fedora31.release"))
        )
    }
}