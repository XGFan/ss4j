package com.tes4x.exp.ss4j.client

import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.socksx.v5.DefaultSocks5InitialRequest
import io.netty.handler.codec.socksx.v5.DefaultSocks5InitialResponse
import io.netty.handler.codec.socksx.v5.Socks5AuthMethod
import org.slf4j.LoggerFactory

@ChannelHandler.Sharable
class Socks5InitialRequestHandler : SimpleChannelInboundHandler<DefaultSocks5InitialRequest>() {

    private val logger = LoggerFactory.getLogger(Socks5InitialRequestHandler::class.java)

    override fun channelRead0(ctx: ChannelHandlerContext, msg: DefaultSocks5InitialRequest) {
        val initialResponse = DefaultSocks5InitialResponse(Socks5AuthMethod.NO_AUTH)
        ctx.writeAndFlush(initialResponse)
    }
}