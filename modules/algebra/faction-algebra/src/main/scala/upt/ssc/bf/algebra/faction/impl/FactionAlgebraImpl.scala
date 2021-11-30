package upt.ssc.bf.algebra.faction.impl

import fs2._
import upt.ssc.bf.algebra.faction.FactionAlgebra
import upt.ssc.bf.core.config.faction.ContractConfig
import upt.ssc.bf.core.model.Contract

import scala.annotation.tailrec

final private[faction] class FactionAlgebraImpl[F[_]: Async: Random](
    contractConfig: ContractConfig
) extends FactionAlgebra[F] {

  override def contractsQueue: Resource[F, Queue[F, Chunk[Contract]]] = for {
    result <- Resource.make(for {
      contractsQueue: Queue[F, Chunk[Contract]] <- Queue
        .bounded[F, Chunk[Contract]](contractConfig.maxChunksInQueue)
      contractsFiber <- generateContracts(contractsQueue).start
    } yield (contractsQueue, contractsFiber)) { case (_, fiber) =>
      fiber.cancel
    }
  } yield result._1

  private def prefixPasswords: Pipe[F, String, String] = { s =>
    val prefixed = contractConfig.prefix match {
      case Some(value) =>
        s.map(value + _)
      case None => s
    }

    prefixed
  }

  @tailrec
  private def generateFromCharset(
      count: Int
  )(charSet: Set[Char])(acc: F[Stream[F, String]]): F[Stream[F, String]] =
    if (count == 0)
      acc
    else {
      val shuffled = shuffleCharset(charSet)

      val next: F[Stream[F, String]] = (acc, shuffled).mapN {
        case (acc, charset) =>
          for {
            s1 <- acc
            c <- charset
          } yield s1 + c
      }
      generateFromCharset(count - 1)(charSet: Set[Char])(next)
    }

  private def generateContracts(
      contractsQueue: Queue[F, Chunk[Contract]]
  ): F[Unit] =
    contractConfig.CHARSET_PREDICTIVE
      .foldLeftM(Set.empty[Char]) { case (charset, c) =>
        for {
          updatedCharset <- (charset + c).pure[F]
          _ <-
            if (charsetNotRespectsConditions(updatedCharset))
              Async[F].unit
            else
              for {
                start <-
                  if (contractConfig.beginsWithAlpha)
                    shuffleCharset(
                      updatedCharset.intersect(contractConfig.ALPHA)
                    ).map(_.map(_.toString))
                  else
                    shuffleCharset(updatedCharset).map(_.map(_.toString))

                // -2 because character c will be inserted at any position in generated passwords
                generated <- generateFromCharset(contractConfig.length - 2)(
                  updatedCharset
                )(start.pure[F])
                containingCharacter = generated.flatMap(s => {
                  val list = (0 to contractConfig.length).map(i =>
                    s.patch(i, c.toString, 0)
                  )
                  Stream.emits(list)
                })
                _ <- containingCharacter
                  .filter(
                    _.toSet
                      .intersect(contractConfig.ALPHA)
                      .size >= contractConfig.minDistinctAlpha.getOrElse(-1)
                  )
                  .filter(
                    _.toSet
                      .intersect(contractConfig.NUM)
                      .size >= contractConfig.minDistinctNum.getOrElse(-1)
                  )
                  .filter(
                    _.toSet
                      .intersect(contractConfig.SYM)
                      .size >= contractConfig.minDistinctSym.getOrElse(-1)
                  )
                  .filter(s =>
                    (s.charAt(0).isLetter && contractConfig.beginsWithAlpha) ||
                      (!s.charAt(0).isLetter && !contractConfig.beginsWithAlpha)
                  )
                  .through(prefixPasswords)
                  .map(Contract.apply)
                  .chunkN(contractConfig.chunkSize)
                  .evalTap(contractsQueue.offer)
                  .compile
                  .drain
              } yield ()

        } yield updatedCharset
      }
      .void

  private def charsetNotRespectsConditions(charset: Set[Char]) =
    charset
      .intersect(contractConfig.ALPHA)
      .size < contractConfig.minDistinctAlpha.getOrElse(-1) || charset
      .intersect(contractConfig.NUM)
      .size < contractConfig.minDistinctNum.getOrElse(-1) || charset
      .intersect(contractConfig.SYM)
      .size < contractConfig.minDistinctSym.getOrElse(-1)

  private def shuffleCharset(charSet: Set[Char]): F[Stream[F, Char]] = Random[F]
    .shuffleList(charSet.toList)
    .map(Stream.emits[F, Char])

}
