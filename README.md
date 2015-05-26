# BottleRocket

BottleRocket is an API to create MongoDB clusters in a variety shapes.  It's intended for use in various testing scenarios where you need
 to test your application or library against various configurations of clusters.  BottleRocket is written in [Kotlin](http://kotlinlang
 .org/) but can be used by any Java project.  In fact, even though the core is written in Kotlin, the tests are written in Java to help 
 ensure the API stays friendly to Java developers while still taking advantage of the language features Kotlin offers.
 
## Installing BottleRocket

### Maven
```xml
<dependency>
    <groupId>com.antwerkz.bottlerocket</groupId>
    <artifactId>bottlerocket</artifactId>
    <version>0.1</version>
 </dependency>
```
  
### Gradle
```groovy
compile 'com.antwerkz.bottlerocket:bottlerocket:0.1`
```

## Using BottleRocket

There are complete examples in the [tests](blob/master/src/test/java/com/antwerkz/bottlerocket/MongoClusterTest.java#L18-18) but using 
BottleRocket is fairly straightforward.  There are three types of clusters you can make:  single node, replica sets, and sharded.

### Single Node

The simplest case is the single node cluster.  While it is not recommended for production use, it's the most common case when testing.  
There are builders available for each of the three cluster types.  For a single node cluster, you would `SingleNode`'s builder:

```java
SingleNode mongod = SingleNode.builder().build();
```

This will create a single `mongod` instance running on port 30000.  `SingleNode` is an instance of `MongoCluster` which provides methods 
to start and shutdown the node as well as a `clean()` which will remove everything from the `SingleNode`'s base directory on down.  It is
 possible to configure the port and the directory where the instance stores its data.  The javadoc for the builder has more details.

### Replica Set

A replica set is similarly easy to configure:

```java
ReplicaSet replicaSet = ReplicaSet.builder().build();
```

This will build a basic three member replica set with member ports starting at 30000.  The name of the replica set can be configured via 
the builder as well as the size of the replica set.

### Sharding

Sharded clusters are also built via builder:

```java
ShardedCluster cluster = ShardedCluster.cluster().build();
```

This builds a sharded cluster with three replica sets each with three members, three config servers, and three mongoses.  The mongos 
isntances will be available starting at the default port of 30000.

## Versions

BottleRocket is intended to work with a number of versions of MongoDB.  The default version to use is "installed" which will use whatever
 version of MongoDB you have installed on your path.  The version to use when building the cluster can be set via which builder is used 
 to build your cluster.  Mixed version clusters are not currently supported via the builders but are on the roadmap.