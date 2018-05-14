package com.tes4x.exp.ss4j.client

import com.tes4x.exp.ss4j.common.TCPBridgeHandler
import io.netty.bootstrap.Bootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.*
import io.netty.channel.socket.DatagramPacket
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioDatagramChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.socksx.v5.*
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import io.netty.util.CharsetUtil.UTF_8
import io.netty.util.NetUtil
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress

@ChannelHandler.Sharable
class SS4JClientHandler : SimpleChannelInboundHandler<DefaultSocks5CommandRequest>() {

    private val logger = LoggerFactory.getLogger(SS4JClientHandler::class.java)

    override fun channelRead0(chc: ChannelHandlerContext, msg: DefaultSocks5CommandRequest) {
        if (msg.type() == Socks5CommandType.CONNECT) {
            val bootstrap = Bootstrap()
            bootstrap.group(chc.channel().eventLoop())
                    .channel(NioSocketChannel::class.java)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(object : ChannelInitializer<SocketChannel>() {
                        override fun initChannel(ch: SocketChannel) {
                            //将SS4J转发给Client
                            ch.pipeline().addLast(TCPBridgeHandler(chc.channel()))
                                    .addLast(Socks5ClientEncoder.DEFAULT)
                        }
                    })
            logger.trace("连接SS4J服务器")
            val future = bootstrap.connect("127.0.0.1", 12080)//todo just for test
            future.addListener(object : ChannelFutureListener {
                override fun operationComplete(cfl: ChannelFuture) {
                    if (cfl.isSuccess) {
                        logger.trace("连接SS4J服务器成功")
                        chc.pipeline()
                                .addLast(TCPBridgeHandler(cfl.channel()))
                        chc.fireChannelRead(msg)
                    } else {
                        logger.trace("连接SS4J服务器失败")
                        chc.writeAndFlush(DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, Socks5AddressType.IPv4))
                    }
                }
            })
        } else if (msg.type() == Socks5CommandType.UDP_ASSOCIATE) {
            logger.trace("UDP无需连接目标服务器")
            val udpServer = Bootstrap()
                    .group(chc.channel().eventLoop())
                    .channel(NioDatagramChannel::class.java)
                    .handler(object : ChannelInitializer<Channel>() {
                        @Throws(Exception::class)
                        override fun initChannel(ch: Channel) {
                            ch.pipeline()
                                    .addLast(LoggingHandler(LogLevel.TRACE))
                                    .addLast(UDPBridgeHandler())
                        }
                    }
                    )
            val future = udpServer.bind(0) // 启动server

            future.addListener {
                ChannelFutureListener { cfl ->
                    val inetSocketAddress = cfl.channel().localAddress() as InetSocketAddress
                    val port = inetSocketAddress.port
                    chc.writeAndFlush(DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, Socks5AddressType.IPv4,
                            (chc.channel().localAddress() as InetSocketAddress).hostString,
                            port))
                }
            }

            chc.pipeline().addLast(object : ChannelInboundHandlerAdapter() {
                @Throws(Exception::class)
                override fun channelInactive(ctx: ChannelHandlerContext) {
                    logger.trace("TCP连接断开，UDP也即将断开")
                    future.channel().close()
                    super.channelInactive(ctx)
                }
            })
        }
    }


    private inner class UDPBridgeHandler : SimpleChannelInboundHandler<DatagramPacket>() {

        private var client: InetSocketAddress? = null

        @Throws(Exception::class)
        override fun channelRead0(ctx: ChannelHandlerContext, msg: DatagramPacket) {
            logger.trace("收到UDP消息! {} {}", msg.sender(), msg.content().toString(UTF_8))

            val content = msg.content()
            val sender = msg.sender()
            if (client == null || client == sender) {
                logger.trace("判断为Client发来的")
                client = sender
                content.skipBytes(2)//skip RSV 保留字段
                val frag = content.readByte()
                val atyp = Socks5AddressType.valueOf(content.readByte())
                var remote: InetSocketAddress? = null
                if (atyp == Socks5AddressType.IPv4) {
                    val ipBytes = ByteArray(4)
                    content.readBytes(ipBytes)
                    val ip = NetUtil.bytesToIpAddress(ipBytes)
                    val port = content.readShort()
                    remote = InetSocketAddress(ip, port.toInt())
                }
                val data = content.readBytes(content.readableBytes())
                ctx.channel().writeAndFlush(DatagramPacket(data, remote))//把消息发到真正的远端
            } else {
                logger.trace("判断为Remote发来的")
                val channel = ctx.channel()

                val fakeHeader = byteArrayOf(0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
                val data = Unpooled.wrappedBuffer(Unpooled.wrappedBuffer(fakeHeader), content.retain())
                channel.writeAndFlush(DatagramPacket(data, client))
            }
        }
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        ctx.close()
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        ctx.close()
//        super.exceptionCaught(ctx, cause)
    }
}
