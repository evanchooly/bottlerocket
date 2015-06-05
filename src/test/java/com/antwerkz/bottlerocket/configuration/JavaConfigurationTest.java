package com.antwerkz.bottlerocket.configuration;

import com.antwerkz.bottlerocket.configuration.blocks.SystemLog;
import com.github.zafarkhaja.semver.Version;
import org.testng.Assert;
import org.testng.annotations.Test;

public class JavaConfigurationTest {
    @Test
    public void testYaml() {
        final Configuration configuration = new Configuration();
        configuration.getSystemLog().setDestination(Destination.SYSLOG);
        configuration.getSystemLog().getComponent().getAccessControl().setVerbosity(Verbosity.FIVE);
        final String target =
            "systemLog:\n" +
            "  component:\n" +
            "    accessControl:\n" +
            "      verbosity: 5\n" +
            "  destination: syslog";
        Assert.assertEquals(configuration.toYaml(Version.valueOf("3.0.3"), ConfigMode.MONGOD), target);
    }

    @Test
    public void complexExample() {
        final Configuration configuration = new Configuration();
        configuration.getStorage().setDbPath("/var/lib/mongodb");
        configuration.getStorage().setRepairPath("/var/lib/mongodb_tmp");
        final SystemLog systemLog = configuration.getSystemLog();
        systemLog.setDestination(Destination.FILE);
        systemLog.setPath("/var/log/mongodb/mongod.log");
        systemLog.setLogAppend(true);
        systemLog.setLogRotate(RotateBehavior.RENAME);
        systemLog.getComponent().getAccessControl().setVerbosity(Verbosity.TWO);
        configuration.getProcessManagement().setFork(true);
        final String target =
            "processManagement:\n" +
            "  fork: true\n" +
            "storage:\n" +
            "  dbPath: /var/lib/mongodb\n" +
            "  repairPath: /var/lib/mongodb_tmp\n" +
            "systemLog:\n" +
            "  component:\n" +
            "    accessControl:\n" +
            "      verbosity: 2\n" +
            "  destination: file\n" +
            "  logAppend: true\n" +
            "  path: /var/log/mongodb/mongod.log" +
            "";
        //              "setParameter:\n" +
        //              "   enableLocalhostAuthBypass: false\n" +
        Assert.assertEquals(configuration.toYaml(Version.valueOf("3.0.3"), ConfigMode.MONGOD), target);
    }
}