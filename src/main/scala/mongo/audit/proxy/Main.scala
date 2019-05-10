package mongo.audit.proxy

package akka_stream

import java.util.UUID

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Source, Tcp}
import akka.util.ByteString

import scala.concurrent.Future
import scala.util.Random

object Main extends App {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val connections: Source[Tcp.IncomingConnection, Future[Tcp.ServerBinding]] =
    Tcp().bind("127.0.0.1", 8888)

  def mongoConnection: Flow[ByteString, ByteString, Future[Tcp.OutgoingConnection]] =
    Tcp().outgoingConnection("127.0.0.1", 27017)

  connections
    .runForeach {
      _.handleWith {
        val id = UUID.randomUUID()

        Flow[ByteString]
          .alsoTo(Audit.auditSink(id))
          .via(mongoConnection)
      }
    }
    .onComplete(_ => system.terminate())
}

