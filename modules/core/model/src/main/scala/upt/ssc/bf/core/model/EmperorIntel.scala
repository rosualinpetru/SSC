package upt.ssc.bf.core.model

import io.circe.generic.JsonCodec

@JsonCodec
case class EmperorIntel(hash: String, salt: String)
