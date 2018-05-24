package com.test4x.ss4j.common

import io.netty.buffer.ByteBuf
import io.netty.handler.codec.socksx.v5.Socks5AddressDecoder
import io.netty.handler.codec.socksx.v5.Socks5AddressEncoder
import io.netty.handler.codec.socksx.v5.Socks5AddressType

class InitConMessage(val dstAddrType: Socks5AddressType, val dstAddr: String, val dstPort: Int) {

//    init {
//        if (dstAddrType === Socks5AddressType.IPv4) {
//            if (!NetUtil.isValidIpV4Address(dstAddr)) {
//                throw IllegalArgumentException("dstAddr: $dstAddr (expected: a valid IPv4 address)")
//            }
//        } else if (dstAddrType === Socks5AddressType.DOMAIN) {
//            dstAddr = IDN.toASCII(dstAddr)
//            if (dstAddr!!.length > 255) {
//                throw IllegalArgumentException("dstAddr: $dstAddr (expected: less than 256 chars)")
//            }
//        } else if (dstAddrType === Socks5AddressType.IPv6) {
//            if (!NetUtil.isValidIpV6Address(dstAddr)) {
//                throw IllegalArgumentException("dstAddr: $dstAddr (expected: a valid IPv6 address")
//            }
//        }
//
//        if (dstPort < 0 || dstPort > 65535) {
//            throw IllegalArgumentException("dstPort: $dstPort (expected: 0~65535)")
//        }
//        this.dstAddr = dstAddr
//    }


    fun write(byteBuf: ByteBuf) {
        byteBuf.writeByte(dstAddrType.byteValue().toInt())
        Socks5AddressEncoder.DEFAULT.encodeAddress(dstAddrType, dstAddr, byteBuf)
        byteBuf.writeShort(dstPort)
    }


    companion object {
        fun fromByteBuf(byteBuf: ByteBuf): InitConMessage {
            val readByte = byteBuf.readByte()
            val type = Socks5AddressType.valueOf(readByte)
            val address = Socks5AddressDecoder.DEFAULT.decodeAddress(type, byteBuf)
            val port = byteBuf.readShort().toInt()
            return InitConMessage(type, address, port)
        }
    }
}
