package com.test4x.ss4j.common

import com.test4x.ss4j.common.TinkConf.aad
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import org.slf4j.LoggerFactory

class SDecryptHandler : ByteToMessageDecoder() {

    private val logger = LoggerFactory.getLogger(SDecryptHandler::class.java)

    override fun decode(ctx: ChannelHandlerContext?, input: ByteBuf, out: MutableList<Any>) {
        logger.trace("----------Decrypt----------")
        val byteArray = ByteArray(input.readableBytes())
        input.readBytes(byteArray)
        out.add(Unpooled.wrappedBuffer(TinkConf.aead.decrypt(byteArray, aad)))
    }

}