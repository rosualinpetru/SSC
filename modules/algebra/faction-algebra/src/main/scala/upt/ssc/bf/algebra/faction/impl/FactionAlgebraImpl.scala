package upt.ssc.bf.algebra.faction.impl

import doobie.{ConnectionIO, _}
import doobie.implicits._
import doobie.util.transactor.Transactor
import fs2.{Pipe, Stream}
import upt.ssc.bf.algebra.faction.FactionAlgebra
import upt.ssc.bf.core.config.faction.ContractConfig
import upt.ssc.bf.core.model._

final private[faction] class FactionAlgebraImpl[
    F[_]: Async: Random: Transactor
](
    contractConfig: ContractConfig
) extends FactionAlgebra[F] {

  override def contractsQueue: Resource[F, Queue[F, Batch]] = for {
    result <- Resource.make(for {
      contractsQueue: Queue[F, Batch] <- Queue
        .bounded[F, Batch](contractConfig.maxChunksInQueue)
      contractsFiber <- generateContracts(contractsQueue).start
    } yield (contractsQueue, contractsFiber)) { case (_, fiber) =>
      fiber.cancel
    }
  } yield result._1

  private val respectsConstraintsPipe: Pipe[F, String, String] =
    _.filterNot(s => {
      val chars = s.toList

      val illegalConditions = List(
        contractConfig.maxAlpha.map(
          _ < chars.intersect(contractConfig.ALPHA.toList).size
        ),
        contractConfig.maxNum.map(
          _ < chars.intersect(contractConfig.NUM.toList).size
        ),
        contractConfig.maxSym.map(
          _ < chars.intersect(contractConfig.SYM.toList).size
        ),
        Some(!contractConfig.canStartWithSym && !s.charAt(0).isLetterOrDigit)
      )

      illegalConditions.flatten.exists(s => s)
    })

  private val generateRandomPassword: F[String] =
    Random[F]
      .shuffleList(contractConfig.CHARSET.toList)
      .map(_.take(contractConfig.length).mkString)

  private def existsInDb: Pipe[F, String, String] = _.evalFilterNot { pass =>
    val program = for {
      exists <- DbQueries.existsPassword(pass)
      _ <- if (exists) 0.pure[ConnectionIO] else DbQueries.insertPassword(pass)
    } yield exists

    program.transact(implicitly[Transactor[F]])
  }

  private def generateContracts(
      contractsQueue: Queue[F, Batch]
  ): F[Unit] = Stream
    .eval(generateRandomPassword)
    .repeat
    .through(respectsConstraintsPipe)
    .through(existsInDb)
    .map(contractConfig.prefix.getOrElse("") + _)
    .map(Contract.apply)
    .chunkN(contractConfig.chunkSize)
    .evalTap(contractsQueue.offer)
    .compile
    .drain

}
