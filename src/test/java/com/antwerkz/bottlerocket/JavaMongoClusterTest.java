package com.antwerkz.bottlerocket;

import com.antwerkz.bottlerocket.clusters.ReplicaSetBuilder;
import com.antwerkz.bottlerocket.executable.Mongod;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;

import java.io.File;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class JavaMongoClusterTest {
    @AfterMethod
    public void sleep() throws InterruptedException {
        Thread.sleep(1000);
    }

    @Test
    public void singleNode() throws InterruptedException, UnknownHostException {
        final SingleNode single = new SingleNode();
        try {
            single.clean();
            single.start();
            final MongoClient client = single.getClient();

            final List<String> names = client.listDatabaseNames().into(new ArrayList<>());
            Assert.assertFalse(names.isEmpty(), names.toString());

            final MongoDatabase db = client.getDatabase("bottlerocket");
            db.drop();
            final MongoCollection<Document> collection = db.getCollection("singlenode");
            final Document document = new Document("key", "value");
            collection.insertOne(document);

            Assert.assertEquals(collection.find().first(), document);
        } finally {
            single.shutdown();
        }
    }

    @Test
    public void replicaSet() {
        final ReplicaSet replicaSet = new ReplicaSet();
        try {
            replicaSet.clean();
            replicaSet.start();

            final MongoClient client = replicaSet.getClient();
            final Mongod primary = replicaSet.getPrimary();
            Assert.assertEquals(primary.getPort(), 30000, "30000 should be the primary at startup");
            final MongoCollection<Document> collection = client.getDatabase("bottlerocket").getCollection("replication");
            final Document document = new Document("key", "value");

            collection
                .withWriteConcern(WriteConcern.ACKNOWLEDGED)
                .insertOne(document);

            final Document first = collection.find().first();
            Assert.assertEquals(document, first);

            Assert.assertTrue(replicaSet.hasPrimary());

            Assert.assertTrue(replicaSet.waitForPrimary() != null);
        } finally {
            replicaSet.shutdown();
        }
    }

    @Test
    public void sharded() {
        final ShardedCluster sharded = new ShardedCluster();
        try {
            sharded.clean();
            sharded.start();
            MongoClient client = sharded.getClient();

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
            sharded.shutdown();
        }
    }

    public void manualCluster() {
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
}