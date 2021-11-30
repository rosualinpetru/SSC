package upt.ssc.bf.core.config.faction

import ciris._
import com.comcast.ip4s._

case class ServerConfig(host: Host, port: Port, threads: Int)

object ServerConfig {

  def load[F[_]: Async]: Resource[F, ServerConfig] = {
    val config = (
      default(host"0.0.0.0"),
      default(port"31513"),
      default(8)
    ).parMapN(ServerConfig.apply)

    Resource.eval(config.load[F])
  }

}
