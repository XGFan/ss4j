package com.test4x.ss4j.server;

import com.test4x.ss4j.common.InitConMessage;
import com.test4x.ss4j.common.TCPBridgeHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SS4JServerHandler extends ChannelInboundHandlerAdapter {
    private boolean epoll;
    private ChannelFuture channelFuture = null;

    private Logger logger = LoggerFactory.getLogger(SS4JServerHandler.class);


    public SS4JServerHandler(boolean isEpoll) {
        this.epoll = isEpoll;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof InitConMessage) {
            channelFuture = new Bootstrap()
                    .group(ctx.channel().eventLoop())
                    .option(ChannelOption.TCP_NODELAY, true)
                    .channel(epoll ? EpollSocketChannel.class : NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast("remote2client", new TCPBridgeHandler(ctx.channel())); //将目标服务器信息转发给客户端

                        }
                    })
                    .connect(((InitConMessage) msg).getDstAddr(), ((InitConMessage) msg).getDstPort());
            channelFuture
                    .addListener((ChannelFutureListener) future -> {
                        if (future.isSuccess()) {
                            final TCPBridgeHandler tcpBridgeHandler = new TCPBridgeHandler(future.channel());
                            ctx.pipeline().addLast("client2remote", tcpBridgeHandler);//将客户端信息转发给目标服务器
                            ctx.pipeline().remove(SS4JServerHandler.this);
                        }
                    });
        } else {
//            if (channelFuture != null) {
                channelFuture.addListener((ChannelFutureListener) f -> {
                    if (f.isSuccess()) {
                        ctx.fireChannelRead(msg);
                    }
                });
//            } else {
//                logger.error("some data discard:{}", msg);
//            }
        }
    }
}
