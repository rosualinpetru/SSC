package upt.ssc.bf.core.model.exception

case object NonexistentEmperorException extends Throwable {
  override def getMessage: String =
    "The environment variables regarding the emperor were not set!"
}
