package com.test4x.ss4j.server

import com.test4x.ss4j.common.aes.DecryptHandler
import com.test4x.ss4j.common.aes.EncryptHandler
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
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import org.slf4j.LoggerFactory


fun main(args: Array<String>) {
    System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "DEBUG")
    val logger = LoggerFactory.getLogger("SS4JServer")

    val config = SS4JConfig("127.0.0.1", listOf(11000, 11001, 11002), 1280)
    val encryptHandler = EncryptHandler(config)

    val serverBootstrap = ServerBootstrap()

    val boss: EventLoopGroup
    val worker: EventLoopGroup
    val channelClass: Class<out ServerChannel>
    if (Epoll.isAvailable()) {
        boss = EpollEventLoopGroup()
        worker = EpollEventLoopGroup()
        channelClass = EpollServerSocketChannel::class.java
        logger.info("Starting Epoll")

    } else {
        boss = NioEventLoopGroup()
        worker = NioEventLoopGroup()
        channelClass = NioServerSocketChannel::class.java
        logger.info("Starting Nio")
    }


    try {
        val bootstrap = serverBootstrap
                .group(boss, worker)
                .channel(channelClass)
                .childHandler(object : ChannelInitializer<Channel>() {
                    override fun initChannel(ch: Channel) {
                        ch.pipeline()
                                .addLast(LoggingHandler(LogLevel.DEBUG))
                                .addLast(encryptHandler)
                                .addLast(DecryptHandler(config))
                                .addLast("InitCon", InitConHandler())
                                .addLast("SS4JS", SS4JServerHandler(Epoll.isAvailable()))
                    }
                })
        val fs = config.serverPort.map {
            bootstrap.bind(it)
        }.map { it.sync() }
        logger.info("SS4J Server Started ${config.serverPort}")
        fs.forEach {
            it.channel().closeFuture().sync()
        }
    } finally {
        boss.shutdownGracefully()
        worker.shutdownGracefully()
    }


}
