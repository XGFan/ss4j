package com.test4x.ss4j.client

import com.test4x.ss4j.common.SS4JConfig
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.EventLoopGroup
import io.netty.channel.ServerChannel
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.epoll.EpollServerSocketChannel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder
import io.netty.handler.codec.socksx.v5.Socks5InitialRequestDecoder
import io.netty.handler.codec.socksx.v5.Socks5ServerEncoder
import org.slf4j.LoggerFactory

fun main(args: Array<String>) {
    System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "DEBUG")
    val logger = LoggerFactory.getLogger("SS4JClient")
    val config = SS4JConfig("127.0.0.1", listOf(11000, 11001, 11002), 1280)

    val serverBootstrap = ServerBootstrap()
    val boss: EventLoopGroup
    val worker: EventLoopGroup
    val channelClass: Class<out ServerChannel>
    val clientHandler: SS4JClientHandler

    if (Epoll.isAvailable()) {
        boss = EpollEventLoopGroup()
        worker = EpollEventLoopGroup()
        channelClass = EpollServerSocketChannel::class.java
        clientHandler = SS4JClientHandler(true, config)
        logger.info("Starting Epoll")

    } else {
        boss = NioEventLoopGroup()
        worker = NioEventLoopGroup()
        channelClass = NioServerSocketChannel::class.java
        clientHandler = SS4JClientHandler(false, config)
        logger.info("Starting Nio")
    }


    val socks5InitialRequestHandler = Socks5InitialRequestHandler()
    try {
        serverBootstrap
                .group(boss, worker)
                .channel(channelClass)
                .localAddress(config.localPort)
                .childHandler(object : ChannelInitializer<Channel>() {
                    override fun initChannel(ch: Channel) {
                        ch.pipeline()
                                .addLast(ProxyIdleHandler())
//                                        .addLast(LoggingHandler(LogLevel.DEBUG))
                                .addLast(Socks5ServerEncoder.DEFAULT)
                                .addLast(Socks5InitialRequestDecoder()) //入站decode
                                .addLast(socks5InitialRequestHandler) //无需验证
                                //socks connection
                                .addLast(Socks5CommandRequestDecoder())
                                //Socks connection
                                .addLast(clientHandler)
                    }

                })
        val future = serverBootstrap.bind().sync()
        logger.info("SS4J Client Started ${config.localPort}")
        future.channel().closeFuture().sync()
    } finally {
        boss.shutdownGracefully()
        worker.shutdownGracefully()
    }
}
