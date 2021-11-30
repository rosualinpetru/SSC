package upt.ssc.bf.algebra.faction.impl

import scala.collection.concurrent.TrieMap
import upt.ssc.bf.algebra.faction.MercenaryCommunicator
import upt.ssc.bf.core.model._

import java.util.UUID

final private[faction] class MercenaryCommunicatorImpl[F[_]: Async](
    communicators: Ref[F, TrieMap[UUID, Queue[F, Batch]]],
    lastBatches: Ref[F, TrieMap[UUID, Option[Batch]]]
) extends MercenaryCommunicator[F] {

  override def enroll(
      mercenaryId: UUID
  ): F[Queue[F, Batch]] = for {
    queue <- Queue.unbounded[F, Batch]
    map <- communicators.get
    _ <- communicators.set(map.addOne(mercenaryId -> queue))
    _ <- setToNone(mercenaryId)
  } yield queue

  override def assignMission(
      mercenaryId: UUID,
      batch: Batch
  ): F[Unit] = for {
    map <- communicators.get
    queue = map.get(mercenaryId)
    _ <- queue match {
      case Some(value) => value.offer(batch)
      case None        => Async[F].unit
    }
    batches <- lastBatches.get
    _ <- lastBatches.set(batches.addOne(mercenaryId -> Some(batch)))
  } yield ()

  override def completeLastMission(mercenaryId: UUID): F[Unit] = setToNone(
    mercenaryId
  )

  override def getLastBatch(mercenaryId: UUID): F[Option[Batch]] = for {
    batches <- lastBatches.get
  } yield batches.get(mercenaryId).flatten

  private def setToNone(mercenaryId: UUID) = for {
    batches <- lastBatches.get
    _ <- lastBatches.set(batches.addOne(mercenaryId -> None))
  } yield ()
}
