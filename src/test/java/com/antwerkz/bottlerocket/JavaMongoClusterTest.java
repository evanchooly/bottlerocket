package com.antwerkz.bottlerocket;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class JavaMongoClusterTest {

    @Test
    public void singleNode() throws InterruptedException, UnknownHostException {
        final SingleNode mongod = SingleNode.builder().build();
        final MongoClient client = new MongoClient("localhost", 30000);
        try {
            mongod.clean();
            mongod.start();

            final List<String> names = client.listDatabaseNames().into(new ArrayList<>());
            Assert.assertFalse(names.isEmpty(), names.toString());

            final MongoDatabase db = client.getDatabase("bottlerocket");
            db.drop();
            final MongoCollection<Document> collection = db.getCollection("singlenode");
            final Document document = new Document("key", "value");
            collection.insertOne(document);

            Assert.assertEquals(collection.find().first(), document);
        } finally {
            client.close();
            mongod.shutdown();
        }
    }

    @Test
    public void replicaSet() {
        final ReplicaSet replicaSet = ReplicaSet.builder().build();
        MongoClient client = null;
        try {
            replicaSet.clean();

            replicaSet.start();

            final Mongod primary = replicaSet.getPrimary();
            Assert.assertEquals(primary.getPort(), 30000, "30000 should be the primary at startup");
            client = replicaSet.getClient();
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
            if (client != null) {
                client.close();
            }
            replicaSet.shutdown();
        }
    }

    @Test
    public void sharded() {
        final ShardedCluster sharded = ShardedCluster.builder()
                                                     .build();
        MongoClient client = null;
        try {
            sharded.clean();
            sharded.start();
            client = new MongoClient(asList(new ServerAddress("localhost", 30000), new ServerAddress("localhost", 30001),
                                            new ServerAddress("localhost", 30002)));

            final ArrayList<Document> list = client.getDatabase("config").getCollection("shards").find().into(new ArrayList<>());
            Assert.assertEquals(list.size(), 3, "Should find 3 shards");
            for (final Document document : list) {
                switch (document.getString("_id")) {
                    case "rocket0":
                        Assert.assertEquals(document.getString("host"), "rocket0/localhost:30003,localhost:30004,localhost:30005");
                        break;
                    case "rocket1":
                        Assert.assertEquals(document.getString("host"), "rocket1/localhost:30006,localhost:30007,localhost:30008");
                        break;
                    case "rocket2":
                        Assert.assertEquals(document.getString("host"), "rocket2/localhost:30009,localhost:30010,localhost:30011");
                        break;
                    default:
                        Assert.fail("found unknown shard member: " + document);
                }
            }
        } finally {
            if (client != null) {
                client.close();
            }
            sharded.shutdown();
        }
    }

    public void manualCluster() {
        final ReplicaSetBuilder builder = ReplicaSet.builder();
        builder.setSize(0);
        final ReplicaSet replicaSet = builder.build();
        replicaSet.clean();

        replicaSet.addNode(new MongoManager("2.6.10").mongod("v2610", 31000, new File("/tmp/v2610"), replicaSet));
        replicaSet.addNode(new MongoManager("3.0.2").mongod("v302", 31001, new File("/tmp/v302"), replicaSet));
        replicaSet.addNode(replicaSet.getMongoManager().mongod("installed", 31002, new File("/tmp/installed"), replicaSet));

        try {
            replicaSet.start();
        } finally {
            replicaSet.shutdown();
        }
    }
}