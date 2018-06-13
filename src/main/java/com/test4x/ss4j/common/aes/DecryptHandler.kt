package com.test4x.ss4j.common.aes

import com.test4x.ss4j.common.SS4JConfig
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import org.slf4j.LoggerFactory

class DecryptHandler(val config: SS4JConfig) : ByteToMessageDecoder() {

    private val logger = LoggerFactory.getLogger(DecryptHandler::class.java)
    val aesGCM = AesGCM(config.key)

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
        if (input.readableBytes() >= (state + AesGCM.GCM_NONCE_LENGTH)) {
            val nonce = ByteArray(AesGCM.GCM_NONCE_LENGTH)
            val byteArray = ByteArray(state)
            input.readBytes(nonce, 0, AesGCM.GCM_NONCE_LENGTH)
            input.readBytes(byteArray, 0, state)
            out.add(Unpooled.wrappedBuffer(aesGCM.decrypt(byteArray, nonce)))
            state = -1
        }
    }


}