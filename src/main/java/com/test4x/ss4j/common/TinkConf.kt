package com.test4x.ss4j.common

import com.google.crypto.tink.*
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.aead.AeadFactory
import com.google.crypto.tink.streamingaead.StreamingAeadConfig
import com.google.crypto.tink.streamingaead.StreamingAeadFactory
import com.google.crypto.tink.streamingaead.StreamingAeadKeyTemplates
import java.io.File


object TinkConf {
    init {
        Config.register(StreamingAeadConfig.TINK_1_1_0)
        Config.register(AeadConfig.TINK_1_1_0)
    }


    val keysetHandle = CleartextKeysetHandle.read(
            JsonKeysetReader.withFile(File("my_keyset.json")))
    val streamKeySetHandle = CleartextKeysetHandle.read(
            JsonKeysetReader.withFile(File("streamKey.json")))


    val streamingAead = StreamingAeadFactory.getPrimitive(streamKeySetHandle)
    val aead = AeadFactory.getPrimitive(keysetHandle)


    val aad = "hello,world".toByteArray()


    fun createNewStreamKey() {

        val keysetHandle = KeysetHandle.generateNew(
                StreamingAeadKeyTemplates.AES128_CTR_HMAC_SHA256_4KB)
        val keysetFilename = "streamKey.json"
        CleartextKeysetHandle.write(keysetHandle, JsonKeysetWriter.withFile(File(keysetFilename)))
    }

}