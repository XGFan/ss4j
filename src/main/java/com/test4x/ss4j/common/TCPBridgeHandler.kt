package com.test4x.ss4j.common

import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import org.slf4j.LoggerFactory

class TCPBridgeHandler(private val channel: Channel) : ChannelInboundHandlerAdapter() {
    private val logger = LoggerFactory.getLogger(TCPBridgeHandler::class.java)


    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val writeAndFlush: ChannelFuture = channel.writeAndFlush(msg)
        writeAndFlush.addListener {
            if (it.isSuccess) {
            } else {
                logger.error("forward fail {}", it.cause())
                throw it.cause()
            }
        }
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        logger.debug("${ctx.name()} inactive")
        channel.close()
        ctx.close()
        ctx.fireChannelInactive()
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        logger.debug("${ctx.name()} exception")
        channel.close()
        ctx.close()
        ctx.fireExceptionCaught(cause)
    }

}