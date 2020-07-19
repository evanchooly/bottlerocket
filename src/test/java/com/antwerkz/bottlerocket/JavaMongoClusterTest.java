package com.antwerkz.bottlerocket;

import com.antwerkz.bottlerocket.BaseTest.Companion;
import com.antwerkz.bottlerocket.clusters.MongoCluster;
import com.antwerkz.bottlerocket.clusters.ReplicaSet;
import com.antwerkz.bottlerocket.clusters.ShardedCluster;
import com.antwerkz.bottlerocket.clusters.SingleNode;
import com.antwerkz.bottlerocket.executable.Mongod;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import static com.antwerkz.bottlerocket.BaseTest.getTimestamp;
import static com.antwerkz.bottlerocket.configuration.ConfigurationKt.configuration;
import static java.lang.String.format;

public class JavaMongoClusterTest {
    @AfterMethod
    void sleep() throws InterruptedException {
        Thread.sleep(1000);
    }

    @Test(enabled = false)
    void singleNode() throws InterruptedException, UnknownHostException {
        final SingleNode cluster = SingleNode.builder()
                                             .baseDir(new File(format("target/rocket-java/%s/singleNode", getTimestamp())).getAbsoluteFile())
                                             .build();
        try {
            startCluster(cluster);
            testWrites(cluster);
        } finally {
            cluster.shutdown();
        }
    }

    @Test(enabled = false)
    void replicaSet() {
        final ReplicaSet cluster = ReplicaSet.builder()
                                             .baseDir(new File(format("target/rocket-java/%s/replicaSet", getTimestamp())))
                                             .build();
        try {
            startCluster(cluster);

            testWrites(cluster);

            final Mongod primary = cluster.getPrimary();
            Assert.assertEquals(primary.getPort(), 30000, "30000 should be the primary at startup");
            Assert.assertTrue(cluster.hasPrimary());
            Assert.assertNotNull(cluster.waitForPrimary());

        } finally {
            cluster.shutdown();
        }
    }

    @Test(enabled = false)
    void sharded() {
        final ShardedCluster cluster = ShardedCluster.builder()
                                                     .baseDir(new File(format("target/rocket-java/%s/sharded", getTimestamp())))
                                                     .build();
        try {
            startCluster(cluster);
            testWrites(cluster);

            final MongoClient client = cluster.getAdminClient();

            final ArrayList<Document> list = client.getDatabase("config")
                                                   .getCollection("shards")
                                                   .find()
                                                   .into(new ArrayList<>());
            Assert.assertEquals(list.size(), 1, "Should find 1 shards");
            for (final Document document : list) {
                if ("rocket0".equals(document.getString("_id"))) {
                    Assert.assertEquals(document.getString("host"), "rocket0/localhost:30001,localhost:30002,localhost:30003");
                } else {
                    Assert.fail("found unknown shard member: " + document);
                }
            }
        } finally {
            cluster.shutdown();
        }
    }

    void startCluster(final MongoCluster cluster) {
        cluster.clean();
        cluster.updateConfig(configuration(c -> {
            c.storage(s -> {
                s.mmapv1(m -> {
                    m.setPreallocDataFiles(false);
                    m.setSmallFiles(true);
                    return null;
                });
                return null;
            });
            return null;
        }));
        cluster.start();
    }

    void testWrites(final MongoCluster cluster) {
        final MongoClient client = cluster.getClient();

        final List<String> names = client.listDatabaseNames().into(new ArrayList<>());
        Assert.assertFalse(names.isEmpty(), names.toString());

        final MongoDatabase db = client.getDatabase("bottlerocket");
        db.drop();
        final MongoCollection<Document> collection = db.getCollection("singlenode");
        final Document document = new Document("key", "value");
        collection.insertOne(document);

        Assert.assertEquals(collection.find().first(), document);
    }

/*
    void mixedCluster() {
        final ReplicaSetBuilder builder = ReplicaSet.builder();
        builder.size(0);
        final ReplicaSet replicaSet = builder.build();
        replicaSet.clean();

        replicaSet.addNode(new MongoManager("2.6.10").mongod("v2610", 31000, new File("/tmp/v2610")));
        replicaSet.addNode(new MongoManager("3.0.2").mongod("v302", 31001, new File("/tmp/v302")));
        replicaSet.addNode(replicaSet.getMongoManager().mongod("installed", 31002, new File("/tmp/installed")));

        try {
            replicaSet.start();
        } finally {
            replicaSet.shutdown();
        }
    }
*/
}