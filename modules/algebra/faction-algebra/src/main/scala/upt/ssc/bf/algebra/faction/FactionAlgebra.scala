package upt.ssc.bf.algebra.faction

import doobie.implicits._
import doobie.util.transactor.Transactor
import upt.ssc.bf.algebra.faction.impl.{DbQueries, FactionAlgebraImpl}
import upt.ssc.bf.core.config.faction.ContractConfig
import upt.ssc.bf.core.model._

trait FactionAlgebra[F[_]] {
  def contractsQueue: Resource[F, Queue[F, Batch]]
}

object FactionAlgebra {
  def apply[F[_]: Async: Random: Transactor](
      contractConfig: ContractConfig
  ): Resource[F, FactionAlgebra[F]] = for {
    _ <- DbQueries.createTable().transact(implicitly[Transactor[F]]).toResource
  } yield new FactionAlgebraImpl[F](contractConfig)

}
