package com.antwerkz.bottlerocket;

import java.io.File;
import java.io.IOException;

import com.github.zafarkhaja.semver.Version;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static java.lang.String.format;

class MongoManagerTest {

    private final MongoManager manager = MongoManager.of("3.0.5");

    @DataProvider(name = "urls")
    Object[][] urls() {
        return new Object[][] {
                           new Object[] { format(MongoManager.macDownload(Version.valueOf("3.0.5")))},
                           new Object[] { format(MongoManager.linuxDownload(Version.valueOf("3.0.5")))},
                           new Object[] { format(MongoManager.windowsDownload(Version.valueOf("3.0.5")))}
        };
    }

    @Test(dataProvider = "urls")
    void testDownload(final String url) throws Exception {
        manager.downloadArchive(url);
        final File file = new File(manager.getDownloadPath(), "mongodb-osx-x86_64-3.0.5");
        Assert.assertTrue(file.isDirectory(), file + " should be a directory");
    }
}