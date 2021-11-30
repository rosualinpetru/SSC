package upt.ssc.bf.core.config.faction

import ciris._

case class ContractConfig(
    length: Int,
    prefix: Option[String],
    minDistinctAlpha: Option[Int],
    minDistinctNum: Option[Int],
    minDistinctSym: Option[Int],
    beginsWithAlpha: Boolean,
    chunkSize: Int,
    maxChunksInQueue: Int
) {
  val ALPHA: Set[Char] = ('a' to 'z').toSet
  val NUM: Set[Char] = ('0' to '9').toSet
  val SYM: Set[Char] = Set('!', '@', '#', '$', '%', '^', '&', '*', '(', ')')

  val CHARSET: Set[Char] = ALPHA ++ NUM ++ SYM

  val CHARSET_PREDICTIVE: List[Char] = List(
    'e', 'a', 'o', 'r', 'i', 'n', 's', 'l', '1', 't', '2', 'm', 'd', 'c', 'y',
    '0', 'b', 'h', 'g', 'u', 'p', '3', 'k', '9', '4', '5', '6', '7', '8', 'f',
    'j', 'w', 'v', 'z', 'x', 'q', '!', '@', '#', '*', '&', '$', '%', '^', '(',
    ')'
  )
}

object ContractConfig {

  def load[F[_]: Async]: Resource[F, ContractConfig] = {
    val config = (
      env("BF_CONTRACT_LENGTH").as[Int].default(7),
      env("BF_CONTRACT_PREFIX").option,
      env("BF_CONTRACT_MIN_DIST_ALPHA").as[Int].option,
      env("BF_CONTRACT_MIN_DIST_NUM").as[Int].option,
      env("BF_CONTRACT_MIN_DIST_SYM").as[Int].option,
      env("BF_CONTRACT_BEGINS_ALPHA").as[Boolean].default(true),
      env("BF_CONTRACT_CHUNK_SIZE").as[Int].default(4096),
      env("BF_CONTRACT_CHUNKS_IN_QUEUE").as[Int].default(5)
    ).parMapN(ContractConfig.apply)

    Resource.eval(config.load[F])
  }

}
