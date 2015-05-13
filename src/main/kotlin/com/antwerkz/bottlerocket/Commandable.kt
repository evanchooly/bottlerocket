package com.antwerkz.bottlerocket

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.mongodb.util.JSON
import com.mongodb.util.JSONParseException
import org.apache.commons.lang3.SystemUtils
import org.bson.BsonDocument
import org.bson.codecs.BsonDocumentCodec
import org.bson.codecs.DecoderContext
import org.bson.json.JsonReader
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException

private val mapper: CustomObjectMapper = CustomObjectMapper()

trait Commandable {
    public fun runCommand(mongod: Mongod, command: String): BsonDocument {
        val stream = ByteArrayOutputStream()
        ProcessExecutor()
              .command(listOf("${mongod.binDir}/${if (SystemUtils.IS_OS_WINDOWS) "mongo.exe" else "mongo"}",
                    "admin", "--port", "${mongod.port}", "--quiet"))
              .redirectOutput(stream)
              .redirectError(Slf4jStream.of(LoggerFactory.getLogger(javaClass<ReplicaSet>())).asInfo())
              .redirectInput(ByteArrayInputStream(command.toByteArray()))
              .execute()

        val json = String(stream.toByteArray()).trim()
        try {
            return BsonDocumentCodec().decode(JsonReader(json), DecoderContext.builder().build())
        } catch(e: Exception) {
            println("json = ${json}")
            throw e;
        }
    }

}

public class ObjectIdSerializer : JsonSerializer<ObjectId>() {
    override
    throws(javaClass<IOException>(), javaClass<JsonProcessingException>())
    public fun serialize( value: ObjectId,  jgen: JsonGenerator,  provider: SerializerProvider)  {
        jgen.writeString(value.toString());
    }
}

public class CustomObjectMapper : ObjectMapper() {
    init {
        val module = SimpleModule("ObjectIdmodule");
        module.addSerializer(javaClass<ObjectId>(), ObjectIdSerializer());
        this.registerModule(module);
    }
}