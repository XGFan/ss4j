package com.test4x.ss4j.common;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TCPBridgeHandler extends ChannelInboundHandlerAdapter {

    private Channel to;

    public TCPBridgeHandler(Channel to) {
        this.to = to;
    }

    final Logger logger = LoggerFactory.getLogger(TCPBridgeHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (to.isWritable()) {
            try {
                to.writeAndFlush(msg).addListener((ChannelFutureListener) future -> {
                    if (!future.isSuccess()) {
                        logger.error("{} Failed from {} to {}", ctx.name(),
                                ctx.channel(), to,
                                future.cause());
                        ctx.disconnect();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                ReferenceCountUtil.release(msg);
            }
        } else {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        to.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        to.close();
    }
}