package com.antwerkz.bottlerocket

import org.apache.commons.lang3.SystemUtils
import org.slf4j.LoggerFactory
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream
import java.io.ByteArrayInputStream

trait Commandable {
    public fun runCommand(mongod: Mongod, command: String): Boolean {
        return ProcessExecutor()
              .command(listOf("${mongod.binDir}/${if (SystemUtils.IS_OS_WINDOWS) "mongo.exe" else "mongo"}",
                    "admin", "--port", "${mongod.port}"))
              .redirectOutput(Slf4jStream.of(LoggerFactory.getLogger(javaClass<ReplicaSet>())).asInfo())
              .redirectError(Slf4jStream.of(LoggerFactory.getLogger(javaClass<ReplicaSet>())).asInfo())
              .redirectInput(ByteArrayInputStream(command.toByteArray()))
              .destroyOnExit()
              .execute()
              .exitValue() == 0
    }

}