package com.test4x.ss4j.common

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import org.slf4j.LoggerFactory

class DecryptHandler : ByteToMessageDecoder() {

    private val logger = LoggerFactory.getLogger(DecryptHandler::class.java)

    var state = -1

    override fun decode(ctx: ChannelHandlerContext, input: ByteBuf, out: MutableList<Any>) {
        if (state == -1) {
            if (input.readableBytes() >= 4) {
                state = input.readInt()
                readRealData(input, out)
            }
        } else {
            readRealData(input, out)
        }
    }

    private fun readRealData(input: ByteBuf, out: MutableList<Any>) {
        if (input.readableBytes() >= state) {
            val byteArray = ByteArray(state)
            input.readBytes(byteArray, 0, state)
            val decrypt = TinkConf.aead.decrypt(byteArray, TinkConf.aad)
            out.add(Unpooled.wrappedBuffer(decrypt))
            state = -1
        }
    }


}