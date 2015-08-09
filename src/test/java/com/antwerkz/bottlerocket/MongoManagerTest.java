package com.antwerkz.bottlerocket;

import java.io.File;
import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static java.lang.String.format;

public class MongoManagerTest {

    private final MongoManager manager = MongoManager.of("3.0.3");

    @DataProvider(name = "urls")
    public Object[][] urls() {
        return new Object[][] {
                           new Object[] { format(MongoManager.macDownload, "3.0.3")},
                           new Object[] { format(MongoManager.linuxDownload, "3.0.3")},
                           new Object[] { format(MongoManager.windowsDownload, "3.0.3")}
        };
    }

    @Test(dataProvider = "urls")
    public void testDownload(final String url) throws Exception {
        manager.downloadArchive(url);
        final File file = new File(manager.getDownloadPath(), "mongodb-osx-x86_64-3.0.2");
        Assert.assertTrue(file.isDirectory(), file + " should be a directory");
    }
}