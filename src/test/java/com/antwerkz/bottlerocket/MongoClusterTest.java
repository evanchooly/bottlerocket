package com.antwerkz.bottlerocket;

//import com.antwerkz.bottlerocket.MongoCluster.ClusterBuilder;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.UnknownHostException;
import java.util.List;

public class MongoClusterTest {
    @Test
    public void singleNode() throws InterruptedException, UnknownHostException {
        Mongod single = new MongoCluster().singleNode();
        single.start();

        MongoClient client = new MongoClient("localhost", 30000);
        final List<String> names = client.getDatabaseNames();
        Assert.assertFalse(names.isEmpty(), names.toString());

        final MongoDatabase db = client.getDatabase("bottlerocket");
        final MongoCollection<Document> collection = db.getCollection("singlenode");
        final Document document = new Document("key", "value");
        collection.insertOne(document);

        Assert.assertEquals(collection.find().first(), document);

        //        Thread.sleep(30000);
//        final MongoCluster cluster = builder.build();
    }
}