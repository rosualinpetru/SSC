package upt.ssc.bf.algebra.faction

import fs2._

import scala.collection.concurrent._
import upt.ssc.bf.algebra.faction.impl.MercenaryCommunicatorImpl
import upt.ssc.bf.core.model.Contract

import java.util.UUID

trait MercenaryCommunicator[F[_]] {
  def enroll(mercenaryId: UUID): F[Queue[F, Chunk[Contract]]]

  def assignContracts(mercenaryId: UUID, batch: Chunk[Contract]): F[Unit]
}

object MercenaryCommunicator {
  def apply[F[_]: Async]: Resource[F, MercenaryCommunicator[F]] = for {
    communicators <- Ref
      .of(TrieMap.empty[UUID, Queue[F, Chunk[Contract]]])
      .toResource
  } yield new MercenaryCommunicatorImpl[F](communicators)
}
