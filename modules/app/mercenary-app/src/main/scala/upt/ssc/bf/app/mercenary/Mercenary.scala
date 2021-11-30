package upt.ssc.bf.app.mercenary

import fs2._
import org.typelevel.log4cats.slf4j.Slf4jLogger
import upt.ssc.bf.app.mercenary.ws.WSMessaging
import upt.ssc.bf.core.config.mercenary.MercenaryConfig
import upt.ssc.bf.core.model.EmperorIntel
import upt.ssc.bf.core.model.ws.MercenaryMessage

object Mercenary extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    Stream.resource(resource[IO]).compile.drain.as(ExitCode.Success)

  def resource[F[_]: Async]: Resource[F, Unit] = for {
    logger <- Slf4jLogger.create[Resource[F, *]]

    config <- MercenaryConfig.load
    _ <- logger.info(s"Config: $config")

    implicit0(intel: EmperorIntel) = config.emperorIntel

    connector <- FactionConnector(
      config.id,
      config.entryCode,
      config.factionLocation
    )
    connection <- connector.connect
    messageQueue <- Queue.unbounded[F, MercenaryMessage].toResource

    wsMessaging <- WSMessaging(connection, messageQueue)
    _ <- wsMessaging.runConnection.toResource
  } yield ()

}
