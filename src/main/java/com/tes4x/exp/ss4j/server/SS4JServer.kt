package com.tes4x.exp.ss4j.server

import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.ByteBuf
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder
import io.netty.handler.codec.socksx.v5.Socks5ServerEncoder
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler

class SS4JServer {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "TRACE")
            val boss = NioEventLoopGroup()
            val worker = NioEventLoopGroup()
            val serverBootstrap = ServerBootstrap()
            val ss4jServer = SS4JServerHandler()
            try {
                serverBootstrap
                        .group(boss, worker)
                        .channel(NioServerSocketChannel::class.java)
                        .localAddress(12080)
                        .childHandler(object : ChannelInitializer<Channel>() {
                            override fun initChannel(ch: Channel) {
                                ch.pipeline()
                                        .addLast(LoggingHandler(LogLevel.TRACE))
//                                        .addLast(object :SimpleChannelInboundHandler<ByteBuf>(){
//                                            override fun channelRead0(ctx: ChannelHandlerContext, msg: ByteBuf) {
//                                                println(msg)
//                                            }
//
//                                        })
                                        .addLast(Socks5ServerEncoder.DEFAULT)

                                        .addLast(Socks5CommandRequestDecoder())
                                        .addLast(ss4jServer)
                            }
                        })
                val future = serverBootstrap.bind().sync()
                future.channel().closeFuture().sync()
            } finally {
                boss.shutdownGracefully()
                worker.shutdownGracefully()
            }
        }
    }
}