package upt.ssc.bf.app.mercenary.ws

import io.circe.syntax.EncoderOps
import io.circe.Decoder
import io.circe.parser.parse
import fs2._
import org.apache.commons.codec.digest.Crypt
import org.http4s.jdkhttpclient.{WSConnectionHighLevel, WSFrame}
import org.typelevel.log4cats.slf4j.Slf4jLogger
import upt.ssc.bf.core.model.{Contract, EmperorIntel}
import upt.ssc.bf.core.model.ws.{
  Available,
  ContractsWS,
  FinishedJob,
  MercenaryMessage
}

import scala.concurrent.duration._

final class WSMessaging[F[_]: Async] private (
    connection: WSConnectionHighLevel[F],
    messageQueue: Queue[F, MercenaryMessage]
)(implicit intel: EmperorIntel) {

  private val logger = Slf4jLogger.getLogger[F]

  def runConnection: F[Unit] = for {
    _ <- messageQueue.offer(Available).delayBy(5.seconds).start
    _ <- logger.info("Running websocket connection!")
    _ <- fromFaction.concurrently(toFaction).compile.drain
  } yield ()

  private val toFaction: Stream[F, Unit] =
    Stream
      .fromQueueUnterminated(messageQueue)
      .map(message => WSFrame.Text(message.asJson.toString()))
      .through(connection.sendPipe)

  private val fromFaction: Stream[F, Unit] = connection.receiveStream
    .evalMap {
      case WSFrame.Text(str, _) =>
        for {
          json <- parse(str).liftTo[F]
          contractsMessage <- Decoder[ContractsWS]
            .decodeJson(json)
            .liftTo[F]

          _ <- logger.debug(
            s"Received batch of contracts! First is: ${contractsMessage.batch.head}"
          )

          t1 <- Clock[F].realTimeInstant
          contracts = contractsMessage.batch.map(Contract.apply)
          emperorOpt <- Stream
            .emits(contracts)
            .parEvalMapUnordered[F, Option[String]](4)(c =>
              Option
                .when(Crypt.crypt(c.password, intel.salt) == intel.hash)(
                  c.password
                )
                .pure[F]
            )
            .compile
            .foldMonoid
          finishedJob = FinishedJob(emperorOpt)
          t2 <- Clock[F].realTimeInstant
          _ <-
            if (emperorOpt.isDefined) logger.warn(s"EMPEROR: ${emperorOpt.get}")
            else Async[F].unit
          _ <- logger.debug(
            s"Took ${t2.getEpochSecond - t1.getEpochSecond} seconds to finish ${contracts.size} contracts"
          )
          _ <- messageQueue.offer(finishedJob)
          _ <- messageQueue.offer(Available)
        } yield ()

      case e =>
        for {
          _ <- logger.warn(s"Unknown frame: $e")
        } yield ()

    }
}

object WSMessaging {
  def apply[F[_]: Async](
      connection: WSConnectionHighLevel[F],
      messageQueue: Queue[F, MercenaryMessage]
  )(implicit intel: EmperorIntel): Resource[F, WSMessaging[F]] =
    new WSMessaging[F](connection, messageQueue).pure[Resource[F, *]]
}
