package com.test4x.ss4j.server

import com.test4x.ss4j.common.DecryptHandler
import com.test4x.ss4j.common.EncryptHandler
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
import org.slf4j.LoggerFactory


fun main(args: Array<String>) {
    System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "Trace")
    val logger = LoggerFactory.getLogger("SS4JServer")

    val serverBootstrap = ServerBootstrap()

    val boss: EventLoopGroup
    val worker: EventLoopGroup
    val channelClass: Class<out ServerChannel>
    val ss4jHandler: SS4JServerHandler
    if (Epoll.isAvailable()) {
        boss = EpollEventLoopGroup()
        worker = EpollEventLoopGroup()
        channelClass = EpollServerSocketChannel::class.java
        ss4jHandler = SS4JServerHandler(true)
        logger.info("Starting Epoll")

    } else {
        boss = NioEventLoopGroup()
        worker = NioEventLoopGroup()
        channelClass = NioServerSocketChannel::class.java
        ss4jHandler = SS4JServerHandler(false)
        logger.info("Starting Nio")
    }


    try {
        serverBootstrap
                .group(boss, worker)
                .channel(channelClass)
                .localAddress(12080)
                .childHandler(object : ChannelInitializer<Channel>() {
                    override fun initChannel(ch: Channel) {
                        ch.pipeline()
//                                .addLast(LoggingHandler(LogLevel.DEBUG))
                                .addLast(EncryptHandler())
                                .addLast(DecryptHandler())
                                .addLast(InitConHandler())
                                .addLast(ss4jHandler)
                    }
                })
        val future = serverBootstrap.bind().sync()
        logger.info("SS4J Server Started")
        future.channel().closeFuture().sync()
    } finally {
        boss.shutdownGracefully()
        worker.shutdownGracefully()
    }


}
