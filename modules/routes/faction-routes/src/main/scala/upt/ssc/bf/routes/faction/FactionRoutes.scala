package upt.ssc.bf.routes.faction

import fs2.concurrent.{SignallingRef, Topic}
import org.http4s._
import org.http4s.dsl._
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.websocket.WebSocketFrame.Close
import org.typelevel.log4cats.slf4j.Slf4jLogger
import upt.ssc.bf.algebra.faction.{FactionAlgebra, MercenaryCommunicator}
import upt.ssc.bf.core.config.EntryCode
import upt.ssc.bf.routes.faction.ws.WSMessenger

final class FactionRoutes[F[_]: Async](entryCode: EntryCode)(
    wsMessenger: WSMessenger[F]
) extends Http4sDsl[F] {

  private val logger = Slf4jLogger.getLogger[F]

  object SecretMatcher extends QueryParamDecoderMatcher[String]("secret")

  def routes(wsb: WebSocketBuilder[F]): HttpRoutes[F] = HttpRoutes.of {
    case GET -> Root / "join" / UUIDVar(mercenaryId) :? SecretMatcher(code) =>
      if (code != entryCode.value)
        for {
          _ <- logger.warn(
            s"Mercenary $mercenaryId attempted to join the faction with code $code but failed!"
          )
          response <- Forbidden()
        } yield response
      else {
        object WSHandler {
          val onClose: F[Unit] = for {
            _ <- logger.info(s"Mercenary $mercenaryId left the faction!")
            lastBatch <- wsMessenger.mercenaryCommunicator.getLastBatch(
              mercenaryId
            )
            _ <- lastBatch match {
              case Some(value) => wsMessenger.contractsQueue.offer(value)
              case None        => Async[F].unit
            }
          } yield ()

          val onNonWSRequest: F[Response[F]] = for {
            _ <- logger.debug(
              s"The request is not a web socket request! ($mercenaryId)"
            )
            response <- BadRequest()
          } yield response

          val onHandshakeFailure: F[Response[F]] = for {
            _ <- logger.debug(
              s"Failure encountered while handshake! ($mercenaryId)"
            )
            response <- InternalServerError()
          } yield response
        }

        for {
          _ <- logger.info(s"Mercenary $mercenaryId joined the faction!")
          mercenaryQueue <- wsMessenger.mercenaryCommunicator.enroll(
            mercenaryId
          )

          response <- wsb
            .withHeaders(Headers.empty)
            .withOnClose(WSHandler.onClose)
            .withOnNonWebSocketRequest(WSHandler.onNonWSRequest)
            .withOnHandshakeFailure(WSHandler.onHandshakeFailure)
            .withFilterPingPongs(false)
            .build(
              send = wsMessenger.toMercenary(mercenaryQueue),
              receive = wsMessenger.fromMercenary(mercenaryId)
            )
        } yield response
      }
  }
}

object FactionRoutes {

  def apply[F[_]: Async](
      factionAlgebra: FactionAlgebra[F],
      mercenaryCommunicator: MercenaryCommunicator[F],
      entryCode: EntryCode
  )(serverSignal: SignallingRef[F, Boolean]): Resource[F, FactionRoutes[F]] =
    for {
      contractsQueue <- factionAlgebra.contractsQueue

      exitTopic <- Topic[F, Close].toResource

      wsHandler <- WSMessenger[F](
        mercenaryCommunicator,
        contractsQueue,
        exitTopic,
        serverSignal
      )

    } yield new FactionRoutes[F](entryCode)(wsHandler)
}
