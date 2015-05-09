package com.antwerkz.bottlerocket;

//import com.antwerkz.bottlerocket.MongoCluster.ClusterBuilder;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.UnknownHostException;
import java.util.List;

public class MongoClusterTest {
    private static final Logger LOG = LoggerFactory.getLogger(MongoClusterTest.class);

    @Test
    public void singleNode() throws InterruptedException, UnknownHostException {
        final Mongod mongod = new MongoCluster().singleNode();
        mongod.start();

        MongoClient client = new MongoClient("localhost", 30000);
        final List<String> names = client.getDatabaseNames();
        Assert.assertFalse(names.isEmpty(), names.toString());

        MongoDatabase db = client.getDatabase("bottlerocket");
        db.drop();
        MongoCollection<Document> collection = db.getCollection("singlenode");
        final Document document = new Document("key", "value");
        collection.insertOne(document);

        Assert.assertEquals(collection.find().first(), document);
        client.close();

        final boolean shutdown = mongod.shutdown();
        final MongoClientOptions options = MongoClientOptions.builder()
                                                             .connectTimeout(1000).build();
        client = new MongoClient(new ServerAddress("localhost", 30000), options);
        try {
            collection = db.getCollection("singlenode");
            Assert.assertEquals(collection.find().first(), document);
            Assert.fail("Connection should have timed out");
        } catch (IllegalStateException e) {
            // expected
        } finally {
            client.close();
        }

        mongod.start();
        client = new MongoClient("localhost", 30000);
        try {
            db = client.getDatabase("bottlerocket");
            collection = db.getCollection("singlenode");
            Assert.assertEquals(collection.find().first(), document);
        } finally {
            client.close();
        }
    }

    @Test
    public void replicaSet() {
        final MongoCluster cluster = new MongoCluster();
        cluster.setReplSetSize(3);
        cluster.setName("rocket");
        cluster.clean();
        final ReplicaSet replicaSet = cluster.replicaSet("rocket");

        replicaSet.start();

        final Mongod primary = replicaSet.getPrimary();
        Assert.assertEquals(primary.getPort(), 30000, "30000 should be the primary at startup");
        final MongoClient client = new MongoClient(new ServerAddress("localhost", 30000));
        final MongoCollection<Document> collection = client.getDatabase("bottlerocket").getCollection("replication");
        final Document document = new Document("key", "value");

        collection
              .withWriteConcern(WriteConcern.REPLICAS_SAFE)
              .insertOne(document);

        final Document first = collection.find().first();
        Assert.assertEquals(document, first);

        Assert.assertTrue(replicaSet.hasPrimary());
    }
}