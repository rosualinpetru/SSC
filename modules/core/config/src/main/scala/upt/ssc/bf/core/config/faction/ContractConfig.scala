package upt.ssc.bf.core.config.faction

import ciris._

case class ContractConfig(
    length: Int,
    prefix: Option[String],
    maxAlpha: Option[Int],
    maxNum: Option[Int],
    maxSym: Option[Int],
    canStartWithSym: Boolean,
    chunkSize: Int,
    maxChunksInQueue: Int
) {
  val ALPHA: Set[Char] = ('a' to 'z').toSet
  val NUM: Set[Char] = ('0' to '9').toSet
  val SYM: Set[Char] = Set('!', '@', '#', '$', '%', '^', '&', '*', '(', ')')

  val CHARSET: Set[Char] = ALPHA ++ NUM ++ SYM
}

object ContractConfig {

  def load[F[_]: Async]: Resource[F, ContractConfig] = {
    val config = (
      env("BF_CONTRACT_LENGTH").as[Int].default(7),
      env("BF_CONTRACT_PREFIX").option,
      env("BF_CONTRACT_MAX_ALPHA").as[Int].option,
      env("BF_CONTRACT_MAX_NUM").as[Int].option,
      env("BF_CONTRACT_MAX_SYM").as[Int].option,
      env("BF_CONTRACT_CAN_START_WITH_SYM").as[Boolean].default(true),
      env("BF_CONTRACT_CHUNK_SIZE").as[Int].default(8192),
      env("BF_CONTRACT_CHUNKS_IN_QUEUE").as[Int].default(5)
    ).parMapN(ContractConfig.apply)

    Resource.eval(config.load[F])
  }

}
