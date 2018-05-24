package com.test4x.ss4j.server

import com.test4x.ss4j.common.InitConMessage
import com.test4x.ss4j.common.TCPBridgeHandler
import io.netty.bootstrap.Bootstrap
import io.netty.channel.*
import io.netty.channel.epoll.EpollSocketChannel
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

@ChannelHandler.Sharable
class SS4JServerHandler(val epoll: Boolean = false) : ChannelInboundHandlerAdapter() {

    val cache: MutableMap<String, List<Any>> = ConcurrentHashMap()
    val status: MutableMap<String, Boolean> = ConcurrentHashMap()

    private val logger = LoggerFactory.getLogger(SS4JServerHandler::class.java)
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val channelId = ctx.channel().id().asLongText()
        if (msg is InitConMessage) {
            val bootstrap = Bootstrap()
            bootstrap.group(ctx.channel().eventLoop())
                    .channel(if (epoll) EpollSocketChannel::class.java else NioSocketChannel::class.java)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(object : ChannelInitializer<SocketChannel>() {
                        override fun initChannel(ch: SocketChannel) {
                            //将目标服务器信息转发给客户端
                            ch.pipeline().addLast("remote2client", TCPBridgeHandler(ctx.channel()))
                        }
                    })
            val future = bootstrap.connect(msg.dstAddr, msg.dstPort)
            future.addListener(object : ChannelFutureListener {
                override fun operationComplete(cfl: ChannelFuture) {
                    if (cfl.isSuccess) {
                        logger.debug("连接Remote服务器成功 {}:{}", msg.dstAddr, msg.dstPort)
                        ctx.pipeline().addLast("client2remote", TCPBridgeHandler(cfl.channel()))
                        logger.debug("ready $channelId")
                        status[channelId] = true
                        (cache[channelId] ?: emptyList()).forEach {
                            ctx.fireChannelRead(it)
                        }
                        ctx.pipeline().remove(this@SS4JServerHandler)
                        logger.debug("remove $channelId")
                        cache.remove(channelId)
                        status.remove(channelId)
                    } else {
                        logger.error("连接Remote服务器失败 {}:{}", msg.dstAddr, msg.dstPort)
                    }
                }
            })
        } else {
            if (status[channelId] != true) {
                logger.debug("save to cache $channelId")
                cache[channelId] = (cache[channelId] ?: emptyList()) + msg
            } else {
                logger.debug("pass to next $channelId")
                ctx.fireChannelRead(msg)
            }
        }
    }


    override fun channelUnregistered(ctx: ChannelHandlerContext) {
        val channelId = ctx.channel().id().asLongText()
        logger.debug("Unregistered $channelId")
        super.channelUnregistered(ctx)
    }


}
