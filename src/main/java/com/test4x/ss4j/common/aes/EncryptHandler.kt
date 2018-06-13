package com.test4x.ss4j.common.aes

import com.test4x.ss4j.common.aes.AesGCM.GCM_NONCE_LENGTH
import com.test4x.ss4j.common.SS4JConfig
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import org.slf4j.LoggerFactory
import java.security.SecureRandom


@ChannelHandler.Sharable
class EncryptHandler(val config: SS4JConfig) : MessageToByteEncoder<ByteBuf>() {
    private val logger = LoggerFactory.getLogger(EncryptHandler::class.java)
    val aesGCM: AesGCM = AesGCM(config.key)
    val random = SecureRandom.getInstanceStrong()


    override fun encode(ctx: ChannelHandlerContext?, msg: ByteBuf, out: ByteBuf) {
        val value = ByteArray(msg.readableBytes())
        msg.readBytes(value)
        val nonce = ByteArray(GCM_NONCE_LENGTH)
        random.nextBytes(nonce)
        val encrypt = aesGCM.encrypt(value, nonce)
        val wrappedBuffer = Unpooled.wrappedBuffer(encrypt)
        out.writeInt(wrappedBuffer.readableBytes())//长度
        out.writeBytes(Unpooled.wrappedBuffer(nonce))//随机字符串
        out.writeBytes(wrappedBuffer)//密文
    }
}