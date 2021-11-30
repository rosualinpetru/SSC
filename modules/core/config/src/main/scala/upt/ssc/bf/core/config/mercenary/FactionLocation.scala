package upt.ssc.bf.core.config.mercenary

import ciris.env

case class FactionLocation(host: String)

object FactionLocation {

  def load[F[_]: Async]: Resource[F, FactionLocation] =
    Resource.eval(
      env("BF_FACTION_HOST")
        .default("localhost:31513")
        .map(FactionLocation.apply)
        .load[F]
    )

}
