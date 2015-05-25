package com.antwerkz.bottlerocket;

import java.io.File;
import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.Test;

public class MongoManagerTest {

    private final MongoManager manager = new MongoManager("installed");

    @Test
    public void testUseInstalled() throws IOException {
        final File mongod = new File("/usr/local/opt/mongodb/bin/mongod");
        if(mongod.exists()) {
            Assert.assertNotNull(manager.useInstalled(), "Should find the homebrew mongo installation");
        }
    }

    @Test
    public void testDownloadMac() throws Exception {

        manager.downloadMac("3.0.2");

        final File file = new File(manager.getDownloadPath(), "mongodb-osx-x86_64-3.0.2");
        Assert.assertTrue(file.isDirectory(), file + " should be a directory");
    }

    @Test
    public void testDownloadLinux() throws Exception {
        manager.downloadLinux("3.0.2");
        final File file = new File(manager.getDownloadPath(), "mongodb-linux-x86_64-3.0.2");
        Assert.assertTrue(file.isDirectory(), file + " should be a directory");
    }

    @Test
    public void testDownloadWindows() throws Exception {
        manager.downloadWindows("3.0.2");
        final File file = new File(manager.getDownloadPath(), "mongodb-win32-x86_64-2008plus-3.0.2");
        Assert.assertTrue(file.isDirectory(), file + " should be a directory");
    }

    @Test
    public void testDownload() throws Exception {
        new MongoManager("3.0.2");
    }
}