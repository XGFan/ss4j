package com.test4x.ss4j.server;

import com.test4x.ss4j.common.InitConMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.socksx.v5.Socks5AddressType;
import io.netty.util.CharsetUtil;
import io.netty.util.NetUtil;

import java.util.List;

public class InitConHandler extends ByteToMessageDecoder {

    private Socks5AddressType type = null;
    private String address = null;
    private short port = Short.MIN_VALUE;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() > 1 && type == null) {
            type = Socks5AddressType.valueOf(in.readByte());
        }
        if (type == null) {
            return;
        }
        if (Socks5AddressType.IPv4.equals(type)) {
            if (in.readableBytes() > 4 && address == null) {
                final byte[] bytes = new byte[4];
                in.readBytes(bytes, 0, 4);
                address = NetUtil.bytesToIpAddress(bytes);
            }
        } else if (Socks5AddressType.IPv6.equals(type)) {
            if (in.readableBytes() > 16 && address == null) {
                final byte[] bytes = new byte[16];
                in.readBytes(bytes, 0, 16);
                address = NetUtil.bytesToIpAddress(bytes);
            }
        } else if (Socks5AddressType.DOMAIN.equals(type)) {
            if (in.readableBytes() > 1 && address == null) {
                final int length = in.getUnsignedByte(in.readerIndex());
                if (in.readableBytes() > length + 1) {
                    in.readUnsignedByte();
                    final ByteBuf byteBuf = in.readBytes(length);
                    address = byteBuf.toString(CharsetUtil.US_ASCII);
                }
            }
        } else {
            return; //unsupported
        }
        if (address == null) {
            return;
        }
        if (in.readableBytes() > 2 && port == Short.MIN_VALUE) {
            port = in.readShort();
        }
        if (port != Short.MIN_VALUE) {
            out.add(new InitConMessage(type, address, port));
            ctx.pipeline().remove(this);
        }
    }
}
