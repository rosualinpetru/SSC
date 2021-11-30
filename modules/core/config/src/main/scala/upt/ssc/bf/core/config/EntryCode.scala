package upt.ssc.bf.core.config

import ciris.env

case class EntryCode(value: String)

object EntryCode {
  def load[F[_]: Async]: Resource[F, EntryCode] =
    Resource.eval(
      env("BF_FACTION_ENTRY_CODE").default("0000").map(EntryCode.apply).load[F]
    )
}
