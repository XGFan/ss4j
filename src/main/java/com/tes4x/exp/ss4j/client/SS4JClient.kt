package com.tes4x.exp.ss4j.client

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder
import io.netty.handler.codec.socksx.v5.Socks5InitialRequestDecoder
import io.netty.handler.codec.socksx.v5.Socks5ServerEncoder
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler

class SS4JClient {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "TRACE")
            val boss = NioEventLoopGroup()
            val worker = NioEventLoopGroup()
            val serverBootstrap = ServerBootstrap()
            val socks5InitialRequestHandler = Socks5InitialRequestHandler()
            val relay = SS4JClientHandler()
            try {
                serverBootstrap
                        .group(boss, worker)
                        .channel(NioServerSocketChannel::class.java)
                        .localAddress(11080)
                        .childHandler(object : ChannelInitializer<Channel>() {
                            override fun initChannel(ch: Channel) {
                                ch.pipeline()
                                        .addLast(ProxyIdleHandler())
                                        .addLast(LoggingHandler(LogLevel.DEBUG))
                                        .addLast(Socks5ServerEncoder.DEFAULT)
                                        //.addLast(new IdleStateHandler(3, 30, 0))
                                        .addLast(Socks5InitialRequestDecoder()) //入站decode
                                        .addLast(socks5InitialRequestHandler) //无需验证
                                        //socks connection
                                        .addLast(Socks5CommandRequestDecoder())
                                        //Socks connection
                                        .addLast(relay)
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