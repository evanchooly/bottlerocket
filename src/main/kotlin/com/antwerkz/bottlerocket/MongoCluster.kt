package com.antwerkz.bottlerocket

import com.mongodb.AuthenticationMechanism
import com.mongodb.MongoClient
import com.mongodb.MongoClientOptions
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import org.bson.codecs.BsonDocumentCodec
import org.bson.codecs.DecoderContext
import org.bson.json.JsonReader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.PosixFilePermission
import java.util.EnumSet

val TEMP_DIR = if (File("/tmp").exists()) "/tmp" else System.getProperty("java.io.tmpdir")

val DEFAULT_MONGOD_NAME = "rocket"
val DEFAULT_PORT = 30000
val DEFAULT_VERSION = "installed"
val DEFAULT_BASE_DIR = File("${TEMP_DIR}/${DEFAULT_MONGOD_NAME}")

public abstract class MongoCluster(public val name: String, public val port: Int, public val version: String, public val baseDir: File) {
    val mongoManager: MongoManager = MongoManager(version)
    private var client: MongoClient? = null;

    abstract fun start();

    open fun shutdown() {
        client?.close()
        client = null;
    }

    abstract fun authEnabled(): Boolean;

    abstract fun enableAuth(pemFile: String = generatePemFile());

    open fun clean() {
        shutdown();
        baseDir.deleteTree()
    }

    fun getClient(): MongoClient {
        if (client == null) {
            val builder = MongoClientOptions.builder()
                  .connectTimeout(3000)
            var credentials = if(authEnabled()) {
                arrayListOf(MongoCredential.createCredential("superuser", "admin", "rocketman".toCharArray()))
            } else {
                listOf<MongoCredential>()
            }
            client = MongoClient(getServerAddressList(), credentials, builder.build())
        }

        return client!!;
    }

    abstract fun getServerAddressList(): List<ServerAddress>

    private fun generatePemFile(): String {
        val pemFile = File(baseDir, "rocket.pem")
        if (!pemFile.exists()) {
            pemFile.getParentFile().mkdirs()
            val stream = FileOutputStream(pemFile)
            try {
                ProcessExecutor()
                      .command(listOf("openssl",  "rand", "-base64", "741"))
                      .redirectOutput(stream)
                      .redirectError(Slf4jStream.of(LoggerFactory.getLogger(javaClass)).asInfo())
                      .execute()
            } finally {
                stream.close()
            }
        }
        Files.setPosixFilePermissions(pemFile.toPath(), EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE))
        return pemFile.getAbsolutePath();
    }
}

fun File.deleteTree() {
    if (exists()) {
        if (isDirectory()) {
            Files.walkFileTree(toPath(), object : SimpleFileVisitor<Path>() {
                override public fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                override public fun postVisitDirectory(dir: Path, e: IOException?): FileVisitResult {
                    if (e == null) {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    } else {
                        // directory iteration failed
                        throw e;
                    }
                }
            });
        } else {
            throw RuntimeException("deleteTree() can only be called on directories:  ${this}")
        }
    }
}
