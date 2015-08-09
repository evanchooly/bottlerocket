package com.antwerkz.bottlerocket;

import com.antwerkz.bottlerocket.executable.Mongod;
import com.mongodb.MongoClient;
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

import static com.antwerkz.bottlerocket.configuration.mongo30.Mongo30Package.configuration;

public class JavaMongoClusterTest {
    @AfterMethod
    public void sleep() throws InterruptedException {
        Thread.sleep(1000);
    }

    @Test
    public void singleNode() throws InterruptedException, UnknownHostException {
        final SingleNode cluster = SingleNode.builder()
                                            .baseDir(new File("build/rocket-java/singleNode"))
                                            .build();
        try {
            startCluster(cluster);
            testWrites(cluster);
        } finally {
            cluster.shutdown();
        }
    }

    @Test
    public void replicaSet() {
        final ReplicaSet cluster = ReplicaSet.builder()
                                                .baseDir(new File("build/rocket-java/replicaSet"))
                                                .build();
        try {
            startCluster(cluster);

            testWrites(cluster);

            final Mongod primary = cluster.getPrimary();
            Assert.assertEquals(primary.getPort(), 30000, "30000 should be the primary at startup");
            Assert.assertTrue(cluster.hasPrimary());
            Assert.assertTrue(cluster.waitForPrimary() != null);

        } finally {
            cluster.shutdown();
        }
    }

    @Test
    public void sharded() {
        final ShardedCluster cluster = ShardedCluster.builder()
                                                     .baseDir(new File("build/rocket-java/sharded"))
                                                     .build();
        try {
            startCluster(cluster);
            testWrites(cluster);

            MongoClient client = cluster.getClient();

            final ArrayList<Document> list = client.getDatabase("config").getCollection("shards").find().into(new ArrayList<>());
            Assert.assertEquals(list.size(), 1, "Should find 1 shards");
            for (final Document document : list) {
                switch (document.getString("_id")) {
                    case "rocket0":
                        Assert.assertEquals(document.getString("host"), "rocket0/localhost:30001,localhost:30002,localhost:30003");
                        break;
                    default:
                        Assert.fail("found unknown shard member: " + document);
                }
            }
        } finally {
            cluster.shutdown();
        }
    }

    public void startCluster(final MongoCluster cluster) {
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

    public void testWrites(final MongoCluster cluster) {
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
    public void mixedCluster() {
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