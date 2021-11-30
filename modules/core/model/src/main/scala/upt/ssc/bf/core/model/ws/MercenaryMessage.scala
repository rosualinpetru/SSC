package upt.ssc.bf.core.model.ws

import io.circe.generic.JsonCodec

@JsonCodec
sealed trait MercenaryMessage

case class FinishedJob(emperorPassword: Option[String])
    extends MercenaryMessage

case object Available extends MercenaryMessage
