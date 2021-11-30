package upt.ssc.bf.core.config.faction

import ciris.{default, env}

//this.apply()
case class DbConfig(
    driver: String,
    jdbcUrl: String,
    username: String,
    password: String,
    poolSize: Int
)

object DbConfig {

  def load[F[_]: Async]: Resource[F, DbConfig] = {
    val config = (
      default("org.postgresql.Driver"),
      env("BF_DB_HOST").default("localhost"),
      env("BF_DB_PORT").as[Int].default(5432),
      env("BF_DB_NAME").default("brute_faction"),
      env("BF_DB_SCHEMA").default("public"),
      env("BF_DB_USERNAME").default("founder"),
      env("BF_DB_PASSWORD").default("founder"),
      env("BF_DB_CONNECTION_POOL").as[Int].default(2)
    ).parMapN {
      case (driver, host, port, name, schema, username, password, poolSize) =>
        DbConfig(
          driver,
          s"jdbc:postgresql://$host:$port/$name?currentSchema=$schema",
          username,
          password,
          poolSize
        )
    }
    Resource.eval(config.load[F])
  }

}
