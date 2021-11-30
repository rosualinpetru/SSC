package upt.ssc.bf.algebra.faction

import scala.collection.concurrent._
import upt.ssc.bf.algebra.faction.impl.MercenaryCommunicatorImpl
import upt.ssc.bf.core.model._

import java.util.UUID

trait MercenaryCommunicator[F[_]] {
  def enroll(mercenaryId: UUID): F[Queue[F, Batch]]

  def assignMission(mercenaryId: UUID, batch: Batch): F[Unit]

  def completeLastMission(mercenaryId: UUID): F[Unit]

  def getLastBatch(mercenaryId: UUID): F[Option[Batch]]
}

object MercenaryCommunicator {
  def apply[F[_]: Async]: Resource[F, MercenaryCommunicator[F]] = for {
    communicators <- Ref
      .of(TrieMap.empty[UUID, Queue[F, Batch]])
      .toResource
    lastBatch <- Ref
      .of(TrieMap.empty[UUID, Option[Batch]])
      .toResource
  } yield new MercenaryCommunicatorImpl[F](communicators, lastBatch)
}
