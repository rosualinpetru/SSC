package upt.ssc.bf.app.mercenary

import org.http4s.Uri
import org.http4s.jdkhttpclient.{
  JdkWSClient,
  WSClient,
  WSConnectionHighLevel,
  WSRequest
}
import upt.ssc.bf.core.config.EntryCode
import upt.ssc.bf.core.config.mercenary.FactionLocation

import java.util.UUID

final class FactionConnector private (
    id: UUID,
    entryCode: EntryCode,
    factionLocation: FactionLocation
) {

  def connect[F[_]: Async]: Resource[F, WSConnectionHighLevel[F]] = {
    import java.net.http.{HttpClient => JClient}

    def createWSClient: Resource[F, WSClient[F]] = {
      val javaClient =
        Resource.make[F, JClient](Async[F].delay(JClient.newBuilder().build()))(
          _ => Async[F].unit
        )
      javaClient.flatMap(client => JdkWSClient[F](client))
    }

    for {
      client <- createWSClient
      uri <- Uri
        .fromString(
          show"ws://${factionLocation.host}/faction/join/$id?secret=${entryCode.value}"
        )
        .liftTo[Resource[F, *]]
      request = WSRequest(uri = uri)
      conn <- client.connectHighLevel(request)
    } yield conn
  }
}

object FactionConnector {
  def apply[F[_]: Async](
      id: UUID,
      entryCode: EntryCode,
      factionLocation: FactionLocation
  ): Resource[F, FactionConnector] =
    new FactionConnector(id, entryCode, factionLocation).pure[Resource[F, *]]
}
