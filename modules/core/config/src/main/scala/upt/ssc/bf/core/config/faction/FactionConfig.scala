package upt.ssc.bf.core.config.faction

import upt.ssc.bf.core.config.EntryCode

case class FactionConfig(
    serverConfig: ServerConfig,
    contractConfig: ContractConfig,
    entryCode: EntryCode
)

object FactionConfig {

  def load[F[_]: Async]: Resource[F, FactionConfig] = for {
    sc <- ServerConfig.load
    cc <- ContractConfig.load
    ec <- EntryCode.load
  } yield FactionConfig(sc, cc, ec)

}
