package upt.ssc.bf.algebra.faction

import fs2._
import upt.ssc.bf.algebra.faction.impl.FactionAlgebraImpl
import upt.ssc.bf.core.config.faction.ContractConfig
import upt.ssc.bf.core.model.Contract

trait FactionAlgebra[F[_]] {
  def contractsQueue: Resource[F, Queue[F, Chunk[Contract]]]
}

object FactionAlgebra {
  def apply[F[_]: Async: Random](
      contractConfig: ContractConfig
  ): Resource[F, FactionAlgebra[F]] =
    new FactionAlgebraImpl[F](contractConfig).pure[Resource[F,*]]
}
