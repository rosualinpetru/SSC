package upt.ssc.bf.core.config.mercenary

import ciris.env
import upt.ssc.bf.core.config.EntryCode
import upt.ssc.bf.core.model.EmperorIntel
import upt.ssc.bf.core.model.exception.NonexistentEmperorException

import java.util.UUID

case class MercenaryConfig(
    id: UUID,
    emperorIntel: EmperorIntel,
    factionLocation: FactionLocation,
    entryCode: EntryCode
)

object MercenaryConfig {

  def load[F[_]: Async]: Resource[F, MercenaryConfig] = for {
    id <- UUIDGen.randomUUID.toResource
    emperorIntel <- loadEmperorIntel[F]
    factionLocation <- FactionLocation.load[F]
    entryCode <- EntryCode.load[F]
  } yield MercenaryConfig(id, emperorIntel, factionLocation, entryCode)

  private def loadEmperorIntel[F[_]: Async]: Resource[F, EmperorIntel] = {
    val config = (
      env("BF_EMPEROR_HASH").option,
      env("BF_EMPEROR_SALT").option
    ).parMapN { case (hashOpt, saltOpt) =>
      for {
        hash <- hashOpt
        salt <- saltOpt
      } yield EmperorIntel(hash, salt)
    }
    Resource.eval(
      config.load[F].flatMap(_.liftTo[F](NonexistentEmperorException))
    )
  }
}
