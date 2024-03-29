package com.antwerkz.bottlerocket.clusters

import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.security.Key
import org.bouncycastle.util.io.pem.PemObject
import org.bouncycastle.util.io.pem.PemWriter

class PemFile(key: Key, description: String?) {
    private val pemObject: PemObject = PemObject(description, key.getEncoded())

    fun write(filename: String) {
        val pemWriter = PemWriter(OutputStreamWriter(FileOutputStream(filename)))
        pemWriter.use { pemWriter -> pemWriter.writeObject(pemObject) }
    }
}
