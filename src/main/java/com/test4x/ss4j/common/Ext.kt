package com.test4x.ss4j.common

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import java.nio.ByteBuffer
import java.nio.channels.ReadableByteChannel


fun ReadableByteChannel.read(dst: ByteBuf): Int {
    var i = 0
    while (true) {
        val temp = Unpooled.wrappedBuffer(ByteBuffer.allocate(1024))
        val cnt = this.read(temp.nioBuffer())
        if (cnt > 0) {
            i += cnt
            dst.writeBytes(temp, cnt)
        } else if (cnt == -1) {
            break
        } else {
            break
        }
    }
    return i
}

fun ByteBuf.read(dst: ByteBuffer): Int {
    val remaining = dst.remaining()
    val readable = this.readableBytes()
    if (readable == 0) {
        return -1
    }
    return if (remaining > readable) {
        repeat(readable) {
            dst.put(this.readByte())
        }
        readable
    } else {
        this.readBytes(dst)
        remaining
    }

}

fun ByteBuf.toChannel(): ReadableByteChannel {
    return object : ReadableByteChannel {

        override fun isOpen(): Boolean {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//            return this@toChannel.readableBytes() > 0
        }

        override fun close() {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun read(dst: ByteBuffer): Int {
            return this@toChannel.read(dst)
        }
    }
}