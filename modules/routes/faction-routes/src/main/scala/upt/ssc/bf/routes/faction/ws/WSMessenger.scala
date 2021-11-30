package upt.ssc.bf.routes.faction.ws

import fs2.concurrent.{SignallingRef, Topic}
import io.circe.syntax.EncoderOps
import fs2.{Pipe, Stream}
import io.circe.Decoder
import io.circe.parser.parse
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame.{Close, Text}
import org.typelevel.log4cats.slf4j.Slf4jLogger
import upt.ssc.bf.algebra.faction.MercenaryCommunicator
import upt.ssc.bf.core.model._
import upt.ssc.bf.core.model.ws.{
  Available,
  ContractsWS,
  FinishedJob,
  MercenaryMessage
}

import java.nio.file.{Path => NioPath}
import scala.concurrent.duration._
import java.util.UUID

private[faction] case class WSMessenger[F[_]: Async] private (
    mercenaryCommunicator: MercenaryCommunicator[F],
    contractsQueue: Queue[F, Batch],
    exitTopic: Topic[F, Close],
    serverSignal: SignallingRef[F, Boolean]
) {

  private val logger = Slf4jLogger.getLogger[F]

  def toMercenary(
      mercenaryQueue: Queue[F, Batch]
  ): Stream[F, WebSocketFrame] = {
    val contractStream = for {
      batch <- Stream.fromQueueUnterminated(mercenaryQueue)
      contracts = ContractsWS(batch.toList.map(_.password))
      json = contracts.asJson
    } yield Text(json.toString())

    contractStream.merge(exitTopic.subscribe(Int.MaxValue))
  }

  def fromMercenary(mercenaryId: UUID): Pipe[F, WebSocketFrame, Unit] =
    _.evalMap {
      case Text(str, _) =>
        for {
          json <- parse(str).liftTo[F]
          message <- Decoder[MercenaryMessage]
            .decodeJson(json)
            .liftTo[F]
          _ <- message match {
            case FinishedJob(Some(password)) =>
              for {
                _ <- logger.warn(
                  s"THE EMPEROR WAS KILLED BY $mercenaryId: $password!"
                )
                _ <- Stream
                  .emit(password)
                  .through(fs2.text.utf8.encode)
                  .through(
                    fs2.io.file
                      .Files[F]
                      .writeAll(
                        fs2.io.file.Path
                          .fromNioPath(NioPath.of("/data/password.txt"))
                      )
                  )
                  .compile
                  .drain
                  .handleErrorWith(_ =>
                    logger.warn(
                      "Could not write password in the according text file. Probably the server does not run in the expected environment!"
                    )
                  )
                _ <- exitTopic.publish1(Close())
                duration = 1.minute
                _ <- logger.warn(s"Closing server in $duration!")
                _ <- serverSignal.set(true).delayBy(duration).start
              } yield ()
            case FinishedJob(None) =>
              for {
                _ <- logger.debug(
                  s"Mercenary $mercenaryId came back from mission!"
                )
                _ <- mercenaryCommunicator.completeLastMission(mercenaryId)
              } yield ()
            case Available =>
              for {
                _ <- logger.info(s"Assign contracts to $mercenaryId!")
                batch <- contractsQueue.take
                _ <- mercenaryCommunicator.assignMission(mercenaryId, batch)
              } yield ()
            case _ => logger.error("Received an inconsistent text frame!")
          }
        } yield ()
      case Close(_) =>
        for {
          _ <- logger.debug(s"Received close frame!")
        } yield ()
      case e =>
        for {
          _ <- logger.warn(s"Unknown frame: $e")
        } yield ()
    }
}

object WSMessenger {
  def apply[F[_]: Async](
      mercenaryCommunicator: MercenaryCommunicator[F],
      contractsQueue: Queue[F, Batch],
      exitTopic: Topic[F, Close],
      serverSignal: SignallingRef[F, Boolean]
  ): Resource[F, WSMessenger[F]] = new WSMessenger[F](
    mercenaryCommunicator,
    contractsQueue,
    exitTopic,
    serverSignal
  ).pure[Resource[F, *]]
}
