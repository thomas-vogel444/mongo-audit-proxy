package mongo.audit.proxy

import java.nio.ByteOrder
import java.util.UUID

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Sink}
import akka.util.ByteString
import akka.util.ByteString.{ByteString1, ByteString1C, ByteStrings}
import mongo.audit.proxy.mongowire.ByteBufferUtils.getBSONCString

import scala.util.Try

object Audit {

  def shouldAudit(byteString: ByteString): Boolean = {

    // decoding Mongo Wire format
    val t = Try {

      val bb = byteString match {
        case byteStrings: ByteStrings => byteStrings.asByteBuffer
        case byteString1: ByteString1 => byteString1.asByteBuffer
        case byteString1C: ByteString1C => byteString1C.asByteBuffer
      }

      bb.order(ByteOrder.LITTLE_ENDIAN)

      // MsgHeader
      val messageLength = bb.getInt()
      val requestID = bb.getInt()
      val responseTo = bb.getInt()
      val opCode = bb.getInt()

      // OP_MSG specifics
      val flagBits = bb.getInt()

      // Sections1
      val s1_kind = bb.get() // Assume it is equal to 0 == a single BSON object
      val s1_size = bb.getInt()

      // Bson document
      val e1_type = bb.get()
      val e1_name = getBSONCString(bb)

      e1_name match {
        case "drop" => true
        case _ => false
      }
    }

    t.getOrElse(false)
  }

  def audit(byteString: ByteString, id: UUID): Unit = {
    println(s"Dropping tables, now are you mister $id?")
  }

  def auditSink(id: UUID): Sink[ByteString, NotUsed] =
    Flow[ByteString]
      .filter(shouldAudit)
      .to(Sink.foreach(byteString=> audit(byteString, id)))

}
