package com.antwerkz.bottlerocket;

import com.jayway.awaitility.Awaitility;
import com.mongodb.MongoClient;
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
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class MongoClusterTest {
    @Test
    public void await() {
        final Callable<Boolean> waiting = () -> {
            System.out.println("waiting");
            return false;
        };
        Awaitility.await()
            .atMost(30, TimeUnit.SECONDS)
            .until(waiting);
    }
    @Test
    public void singleNode() throws InterruptedException, UnknownHostException {
        final SingleNodeBuilder builder = SingleNode.builder();
        builder.setName("rocket");
        final SingleNode mongod = builder.build();
        try {
            mongod.clean();
            mongod.start();

            final MongoClient client = new MongoClient("localhost", 30000);
            final List<String> names = client.getDatabaseNames();
            Assert.assertFalse(names.isEmpty(), names.toString());

            final MongoDatabase db = client.getDatabase("bottlerocket");
            db.drop();
            final MongoCollection<Document> collection = db.getCollection("singlenode");
            final Document document = new Document("key", "value");
            collection.insertOne(document);

            Assert.assertEquals(collection.find().first(), document);
            client.close();
        } finally {
            mongod.shutdown();
        }
    }

    @Test
    public void replicaSet() {
        final ReplicaSetBuilder builder = ReplicaSet.builder();
        builder.setName("rocket");
        final ReplicaSet replicaSet = builder.build();
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

    @Test
    public void sharded() {
        final ShardedCluster sharded = ShardedCluster.cluster()
            .build();
        sharded.clean();
        sharded.start();
    }

}