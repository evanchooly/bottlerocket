package com.antwerkz.bottlerocket.configuration;

import com.antwerkz.bottlerocket.configuration.mongo30.Configuration;
import com.antwerkz.bottlerocket.configuration.types.Destination;
import com.antwerkz.bottlerocket.configuration.types.RotateBehavior;
import com.antwerkz.bottlerocket.configuration.types.Verbosity;
import org.testng.Assert;
import org.testng.annotations.Test;

import static com.antwerkz.bottlerocket.configuration.mongo30.ConfigurationKt.configuration;

class JavaConfigurationTest {
    @Test
    void testYaml() {
        final Configuration configuration = configuration(c -> {
            c.systemLog(s -> {
                s.setDestination(Destination.SYSLOG);
                s.component(comp -> {
                    comp.accessControl(a -> {
                        a.setVerbosity(Verbosity.FIVE);
                        return null;
                    });
                    return null;
                });

                return null;
            });
            return null;
        });
        final String target =
            "net:\n" +
            "  bindIp: 127.0.0.1\n" +
            "  port: 27017\n" +
            "replication:\n" +
            "  oplogSizeMB: 10\n" +
            "storage:\n" +
            "  mmapv1:\n" +
            "    preallocDataFiles: false\n" +
            "    smallFiles: true\n" +
            "systemLog:\n" +
            "  component:\n" +
            "    accessControl:\n" +
            "      verbosity: 5\n" +
            "  destination: syslog" +
            "\n";
        Assert.assertEquals(configuration.toYaml(ConfigMode.MONGOD, false), target);
    }

    @Test
    void complexExample() {
        final Configuration configuration =
            configuration(c -> {
                c.storage(s -> {
                    s.setDbPath("/var/lib/mongodb");
                    s.setRepairPath("/var/lib/mongodb_tmp");
                    return null;
                });
                c.systemLog(s -> {
                    s.setDestination(Destination.FILE);
                    s.setPath("/var/log/mongodb/mongod.log");
                    s.setLogAppend(true);
                    s.setLogRotate(RotateBehavior.RENAME);

                    s.component(component -> {
                        component.accessControl(a -> {
                            a.setVerbosity(Verbosity.TWO);
                            return null;
                        });
                        return null;
                    });
                    return null;
                });
                c.processManagement(p -> {
                    p.setFork(true);
                    return null;
                });
                return null;
            });

        final String target =
            "net:\n" +
            "  bindIp: 127.0.0.1\n" +
            "  port: 27017\n" +
            "processManagement:\n" +
            "  fork: true\n" +
            "replication:\n" +
            "  oplogSizeMB: 10\n" +
            "storage:\n" +
            "  dbPath: /var/lib/mongodb\n" +
            "  mmapv1:\n" +
            "    preallocDataFiles: false\n" +
            "    smallFiles: true\n" +
            "  repairPath: /var/lib/mongodb_tmp\n" +
            "systemLog:\n" +
            "  component:\n" +
            "    accessControl:\n" +
            "      verbosity: 2\n" +
            "  destination: file\n" +
            "  logAppend: true\n" +
            "  logRotate: rename\n" +
            "  path: /var/log/mongodb/mongod.log" +
            "\n";
        //              "setParameter:\n" +
        //              "   enableLocalhostAuthBypass: false\n" +
        Assert.assertEquals(configuration.toYaml(ConfigMode.MONGOD, false), target);
    }

    @Test
    void testComplexBuilder() {
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
                s.setLogRotate(RotateBehavior.RENAME);
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

//        Assert.assertEquals(config.toYaml(ConfigMode.MONGOD, false),
//            ConfigurationTest.complexConfig);
    }
}