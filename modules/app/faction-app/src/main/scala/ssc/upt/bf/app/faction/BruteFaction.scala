package ssc.upt.bf.app.faction

import fs2.Stream

object BruteFaction extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    Stream.resource(resource[IO]).compile.drain.as(ExitCode.Success)

  def resource[F[_]: Async]: Resource[F, Unit] =
    for {
      founder <- Founder[F]
      _ <- founder.found
    } yield ()
}
