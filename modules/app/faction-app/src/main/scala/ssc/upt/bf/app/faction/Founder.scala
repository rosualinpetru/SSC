package ssc.upt.bf.app.faction

import fs2.concurrent.SignallingRef
import org.http4s.blaze.server._
import org.http4s.server._
import org.typelevel.log4cats.slf4j.Slf4jLogger
import upt.ssc.bf.core.config.faction.FactionConfig
import upt.ssc.bf.routes.faction.FactionRoutes

final case class Founder[F[_]: Async] private (factionConfig: FactionConfig)(
    signal: SignallingRef[F, Boolean],
    exitCode: Ref[F, ExitCode]
) {

  private val logger = Slf4jLogger.getLogger[F].mapK(Resource.liftK[F])

  private implicit val r: Random[F] =
    Random.javaUtilConcurrentThreadLocalRandom[F]

  private def bindHttp4sServer(
      factionRoutes: FactionRoutes[F]
  ): Resource[F, Unit] =
    BlazeServerBuilder[F]
      .withConnectorPoolSize(factionConfig.serverConfig.threads)
      .bindHttp(
        factionConfig.serverConfig.port.value,
        factionConfig.serverConfig.host.toString
      )
      .withHttpWebSocketApp(builder =>
        Router("/faction" -> factionRoutes.routes(builder)).orNotFound
      )
      .withoutBanner
      .serveWhile(signal, exitCode)
      .compile
      .resource
      .drain

  def found: Resource[F, Unit] = for {

    _ <- logger.info(s"Config: $factionConfig")

    factionRoutes <- FactionRoutes(
      factionConfig.contractConfig,
      factionConfig.entryCode
    )(signal)

    _ <- bindHttp4sServer(factionRoutes)
  } yield ()

}

object Founder {
  def apply[F[_]: Async]: Resource[F, Founder[F]] = for {
    factionConfig <- FactionConfig.load[F]
    signal <- SignallingRef[F, Boolean](false).toResource
    exitCode <- Ref[F].of(ExitCode.Success).toResource
  } yield Founder(factionConfig)(signal, exitCode)
}
