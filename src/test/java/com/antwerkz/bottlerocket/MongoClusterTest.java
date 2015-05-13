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
        final Mongod mongod = MongoCluster.Companion.singleNode("rocket", 30000, "installed");
        try {
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

/*
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
*/
        } finally {
            mongod.shutdown();
        }
    }

    @Test
    public void replicaSet() {
        final ReplicaSet replicaSet = MongoCluster.Companion.replicaSet("rocket", 30000, "installed", 3);
        try {
            replicaSet.clean();

            replicaSet.start();

            final Mongod primary = replicaSet.getPrimary();
            Assert.assertEquals(primary.getPort(), 30000, "30000 should be the primary at startup");
            final MongoClient client = replicaSet.getClient();
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
}