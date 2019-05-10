package mongo.audit.proxy.mongowire

import java.nio.ByteBuffer

import scala.collection.mutable.ArrayBuffer
import scala.util.control.Breaks

object ByteBufferUtils {
  // Modifies the position of the byte buffer
  def getBSONCString(byteBuffer: ByteBuffer): String = {
    val arrayBuffer = new ArrayBuffer[Byte](20)
    Breaks.breakable {
      while (true) {
        val nextByte: Byte = byteBuffer.get()

        if (nextByte == 0.toByte) {
          Breaks.break()
        } else {
          arrayBuffer.append(nextByte)
        }
      }
    }

    new String(arrayBuffer.toArray)
  }

  def getBSONString(byteBuffer: ByteBuffer): String = {
    val stringSize = byteBuffer.getInt()
    println(s"stringSize: $stringSize")

    println(stringSize.toHexString)
    val byteArray = new Array[Byte](stringSize)
    byteBuffer.get(byteArray)

    byteBuffer.get() // Remove the trailing null byte

    new String(byteArray)
  }
}
