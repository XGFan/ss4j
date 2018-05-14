package com.tes4x.exp.ss4j.common

import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import org.slf4j.LoggerFactory

class TCPBridgeHandler(private val channel: Channel) : ChannelInboundHandlerAdapter() {
    private val logger = LoggerFactory.getLogger(TCPBridgeHandler::class.java)


    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        logger.trace("Forward {} to {}", ctx.channel(), channel)
        val writeAndFlush: ChannelFuture = channel.writeAndFlush(msg)
        writeAndFlush.addListener {
            if (it.isSuccess) {
                logger.trace("forward success")
            } else {
                logger.trace("forward fail")
                throw it.cause()
            }
        }
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        channel.close()
    }


//    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
////        logger.error("", cause)
//        channel.close()
//    }
}