//package com.tes4x.exp.ss4j.server
//
//import io.netty.buffer.ByteBuf
//import io.netty.channel.ChannelHandlerContext
//import io.netty.handler.codec.DecoderException
//import io.netty.handler.codec.DecoderResult
//import io.netty.handler.codec.ReplayingDecoder
//import io.netty.handler.codec.socksx.SocksVersion
//import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandRequest
//import io.netty.handler.codec.socksx.v5.Socks5AddressDecoder
//import io.netty.handler.codec.socksx.v5.Socks5AddressType
//import io.netty.handler.codec.socksx.v5.Socks5CommandType
//
//class Socks5Decoder(val addressDecoder: Socks5AddressDecoder = Socks5AddressDecoder.DEFAULT) : ReplayingDecoder<Any>() {
//    enum class sta
//
//
//    @Throws(Exception::class)
//    override fun decode(ctx: ChannelHandlerContext, `in`: ByteBuf, out: MutableList<Any>) {
//        try {
//            when (state()) {
//                io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder.State.INIT -> {
//                    run {
//                        val version = `in`.readByte()
//                        if (version != SocksVersion.SOCKS5.byteValue()) {
//                            throw DecoderException(
//                                    "unsupported version: " + version + " (expected: " + SocksVersion.SOCKS5.byteValue() + ')'.toString())
//                        }
//
//                        val type = Socks5CommandType.valueOf(`in`.readByte())
//                        `in`.skipBytes(1) // RSV
//                        val dstAddrType = Socks5AddressType.valueOf(`in`.readByte())
//                        val dstAddr = addressDecoder.decodeAddress(dstAddrType, `in`)
//                        val dstPort = `in`.readUnsignedShort()
//
//                        out.add(DefaultSocks5CommandRequest(type, dstAddrType, dstAddr, dstPort))
//                        checkpoint(io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder.State.SUCCESS)
//                    }
//                    run {
//                        val readableBytes = actualReadableBytes()
//                        if (readableBytes > 0) {
//                            out.add(`in`.readRetainedSlice(readableBytes))
//                        }
//                        break
//                    }
//                }
//                io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder.State.SUCCESS -> {
//                    val readableBytes = actualReadableBytes()
//                    if (readableBytes > 0) {
//                        out.add(`in`.readRetainedSlice(readableBytes))
//                    }
//                }
//                io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder.State.FAILURE -> {
//                    `in`.skipBytes(actualReadableBytes())
//                }
//            }
//        } catch (e: Exception) {
//            fail(out, e)
//        }
//
//    }
//
//    private fun fail(out: MutableList<Any>, cause: Exception) {
//        var cause = cause
//        if (cause !is DecoderException) {
//            cause = DecoderException(cause)
//        }
//
//        checkpoint(io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder.State.FAILURE)
//
//        val m = DefaultSocks5CommandRequest(
//                Socks5CommandType.CONNECT, Socks5AddressType.IPv4, "0.0.0.0", 1)
//        m.setDecoderResult(DecoderResult.failure(cause))
//        out.add(m)
//    }
//}