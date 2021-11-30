package upt.ssc.bf.algebra.faction.impl

import fs2.Chunk

import scala.collection.concurrent.TrieMap
import upt.ssc.bf.algebra.faction.MercenaryCommunicator
import upt.ssc.bf.core.model.Contract

import java.util.UUID

final private[faction] class MercenaryCommunicatorImpl[F[_]: Async](
    communicators: Ref[
      F,
      TrieMap[UUID, Queue[F, Chunk[Contract]]]
    ]
) extends MercenaryCommunicator[F] {

  override def enroll(
      mercenaryId: UUID
  ): F[Queue[F, Chunk[Contract]]] = for {
    queue <- Queue.unbounded[F, Chunk[Contract]]
    map <- communicators.get
    _ <- communicators.set(map.addOne(mercenaryId -> queue))
  } yield queue

  override def assignContracts(
      mercenaryId: UUID,
      batch: Chunk[Contract]
  ): F[Unit] = for {
    map <- communicators.get
    queue = map.get(mercenaryId)
    _ <- queue match {
      case Some(value) => value.offer(batch)
      case None        => Async[F].unit
    }
  } yield ()

}
