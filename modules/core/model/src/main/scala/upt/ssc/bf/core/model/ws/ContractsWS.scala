package upt.ssc.bf.core.model.ws

import io.circe.generic.JsonCodec

@JsonCodec
case class ContractsWS(batch: List[String])
