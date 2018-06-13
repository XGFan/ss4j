package com.test4x.ss4j.common

import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import javax.crypto.spec.SecretKeySpec

data class SS4JConfig(val serverHost: String,
                      val serverPort: List<Int> = listOf(4000),
                      val localPort: Int = 1180) {

    val key: SecretKeySpec

    init {
        val secret = Paths.get("secret")
        val bytes = Files.readAllBytes(secret)
        key = SecretKeySpec(bytes, 0, bytes.size, "AES")
    }

    private val random = Random()

    val randomPort: Int
        get() = serverPort[random.nextInt(serverPort.size)]


    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val config = SS4JConfig("127.0.0.1", listOf(4000, 4100, 4200), 1280)
            repeat(10000) {
                println(config.randomPort)
            }
        }
    }
}