package com.test4x.ss4j.common

import com.test4x.ss4j.common.TinkConf.aad
import com.test4x.ss4j.common.TinkConf.aead
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import org.slf4j.LoggerFactory


class EncryptHandler : MessageToByteEncoder<ByteBuf>() {
    private val logger = LoggerFactory.getLogger(EncryptHandler::class.java)

    override fun encode(ctx: ChannelHandlerContext?, msg: ByteBuf, out: ByteBuf) {
        val value = ByteArray(msg.readableBytes())
        msg.readBytes(value)
        val encrypt = aead.encrypt(value, aad)
        val wrappedBuffer = Unpooled.wrappedBuffer(encrypt)
        out.writeInt(wrappedBuffer.readableBytes())
        out.writeBytes(wrappedBuffer)
    }
}