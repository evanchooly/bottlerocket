package com.antwerkz.bottlerocket.configuration;

import com.antwerkz.bottlerocket.configuration.blocks.SystemLog;
import com.github.zafarkhaja.semver.Version;
import org.testng.Assert;
import org.testng.annotations.Test;

import static com.antwerkz.bottlerocket.configuration.ConfigurationPackage.configuration;
import static com.antwerkz.bottlerocket.configuration.ConfigurationTest.COMPLEX_CONFIG;

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

    @Test
    public void testComplexBuilder() {
        final Configuration config = configuration(c -> {
            c.processManagement(p -> {
                p.setFork(true);
                return null;
            });
            c.storage(s -> {
                s.setDbPath("/var/lib/mongodb");
                s.setRepairPath("/var/lib/mongodb_tmp");
                return null;
            });
            c.systemLog(s -> {
                s.component(component -> {
                    component.accessControl(a -> {
                        a.setVerbosity(Verbosity.TWO);
                        return null;
                    });
                    return null;
                });
                s.setDestination(Destination.FILE);
                s.setLogAppend(true);
                s.setPath("/var/log/mongodb/mongod.log");
                return null;
            });
            return null;
        });

/*
        String target = "processManagement:\n" +
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
                        "  path: /var/log/mongodb/mongod.log";
        //              "setParameter:\n" +
        //              "   enableLocalhostAuthBypass: false\n" +
*/

        Assert.assertEquals(config.toYaml(Version.valueOf("3.0.0"), ConfigMode.MONGOD), COMPLEX_CONFIG);
    }

}