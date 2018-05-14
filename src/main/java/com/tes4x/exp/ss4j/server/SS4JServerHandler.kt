package com.tes4x.exp.ss4j.server

import com.tes4x.exp.ss4j.common.TCPBridgeHandler
import io.netty.bootstrap.Bootstrap
import io.netty.channel.*
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.socksx.v5.*
import org.slf4j.LoggerFactory

@ChannelHandler.Sharable
class SS4JServerHandler : SimpleChannelInboundHandler<DefaultSocks5CommandRequest>() {
    private val logger = LoggerFactory.getLogger(SS4JServerHandler::class.java)

    override fun channelRead0(ctx: ChannelHandlerContext, msg: DefaultSocks5CommandRequest) {
        if (msg.type() == Socks5CommandType.CONNECT) {
            val bootstrap = Bootstrap()
            bootstrap.group(ctx.channel().eventLoop())
                    .channel(NioSocketChannel::class.java)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(object : ChannelInitializer<SocketChannel>() {
                        override fun initChannel(ch: SocketChannel) {
                            //将目标服务器信息转发给客户端
                            ch.pipeline().addLast(TCPBridgeHandler(ctx.channel()))
                        }
                    })
            val future = bootstrap.connect(msg.dstAddr(), msg.dstPort())
            future.addListener(object : ChannelFutureListener {
                override fun operationComplete(cfl: ChannelFuture) {
                    if (cfl.isSuccess) {
                        logger.trace("连接Remote服务器成功 {}:{}", msg.dstAddr(), msg.dstPort())
                        ctx.pipeline().addLast(TCPBridgeHandler(cfl.channel()))
                        ctx.writeAndFlush(DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, Socks5AddressType.IPv4)).addListener {
                            if (!it.isSuccess) {
                                throw it.cause()
                            }
                        }
                    } else {
                        logger.trace("连接Remote服务器失败 {}:{}", msg.dstAddr(), msg.dstPort())
                        ctx.writeAndFlush(DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, Socks5AddressType.IPv4))
                    }
                }
            })
        }
    }
}
