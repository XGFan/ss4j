import com.test4x.ss4j.common.TinkConf.aad
import com.test4x.ss4j.common.TinkConf.streamingAead
import com.test4x.ss4j.common.read
import com.test4x.ss4j.common.toChannel
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets


fun main(args: Array<String>) {
    val s = "我真他妈是日了狗了"
    val byteArray = s.toByteArray(StandardCharsets.UTF_8)

//
//    val bos = ByteArrayOutputStream()
//
//
//    val encrypt = aead.encrypt(byteArray, aad)
//    println(String(encrypt))
//
//    val sliceArray = encrypt.sliceArray(IntRange(0, 50))
//    println(aead.decrypt(sliceArray, aad))


//    val newEncryptingStream = streamingAead.newEncryptingChannel(FileOutputStream("wtf").channel, aad)
//    newEncryptingStream.write(ByteBuffer.wrap(byteArray))
//    newEncryptingStream.close()
//
//
    val encryptedByteArray = FileInputStream("wtf").readBytes()
    val a1 = encryptedByteArray.sliceArray(IntRange(0, 10))
    val a2 = encryptedByteArray.sliceArray(IntRange(10, 27))
    val toChannel = Unpooled.wrappedBuffer(a1).toChannel()


    val newDecryptingChannel = streamingAead.newDecryptingChannel(toChannel, aad)
    val buffer = Unpooled.buffer()
    val read = newDecryptingChannel.read(buffer)
    val ba = ByteArray(buffer.readableBytes())
    buffer.readBytes(ba)
    println(String(ba))


//
//    val wrappedBuffer = Unpooled.wrappedBuffer(byteArray)
//    println(wrappedBuffer)
//
//    val allocate = ByteBuffer.allocate(5)
//    wrappedBuffer.read(allocate)
//    println(wrappedBuffer)
//    println(allocate)
//
//    allocate.clear()
//    wrappedBuffer.read(allocate)
//    println(wrappedBuffer)
//    println(allocate)
}


fun ByteBuffer.copyToBuf(dst: ByteBuf) {
//    this.
}