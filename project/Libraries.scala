import sbt._

trait Libraries {
  // https://mvnrepository.com/artifact/co.fs2/fs2-core
  protected val fs2CoreDep = "co.fs2" %% "fs2-core" % "3.2.2" withSources ()

  // https://mvnrepository.com/artifact/co.fs2/fs2-io
  protected val fs2IODep = "co.fs2" %% "fs2-io" % "3.2.2" withSources ()

  // https://mvnrepository.com/artifact/org.typelevel/cats-core
  protected val catsDep = "org.typelevel" %% "cats-core" % "2.6.1" withSources ()

  // https://mvnrepository.com/artifact/org.typelevel/cats-effect
  protected val catsEffectDep =
    "org.typelevel" %% "cats-effect" % "3.3-393-da7c7c7" withSources ()

  // https://mvnrepository.com/artifact/org.typelevel/log4cats-core
  protected val log4catsCoreDep =
    "org.typelevel" %% "log4cats-core" % "2.1.1" withSources ()

  // https://mvnrepository.com/artifact/org.typelevel/log4cats-slf4j
  protected val log4catsSlf4jDep =
    "org.typelevel" %% "log4cats-slf4j" % "2.1.1" withSources ()

  // https://mvnrepository.com/artifact/org.http4s/http4s-dsl
  protected val http4sDSLDep = "org.http4s" %% "http4s-dsl" % "1.0.0-M29"

  // https://mvnrepository.com/artifact/org.http4s/http4s-jdk-http-client
  protected val http4sJdkHttpClientDep =
    "org.http4s" %% "http4s-jdk-http-client" % "0.6.0-M6"

  // https://mvnrepository.com/artifact/org.http4s/http4s-blaze-server
  protected val htt4psBlazeServerDep =
    "org.http4s" %% "http4s-blaze-server" % "1.0.0-M29"

  // https://mvnrepository.com/artifact/org.http4s/http4s-circe
  protected val http4sCirceDep = "org.http4s" %% "http4s-circe" % "1.0.0-M29"

  // https://mvnrepository.com/artifact/io.circe/circe-core
  protected val circeCoreDep = "io.circe" %% "circe-core" % "0.14.1" withSources ()

  // https://mvnrepository.com/artifact/io.circe/circe-parser
  protected val circeParserDep = "io.circe" %% "circe-parser" % "0.14.1" withSources ()

  // https://mvnrepository.com/artifact/io.circe/circe-generic
  protected val circeGenericDep =
    "io.circe" %% "circe-generic" % "0.14.1" withSources ()

  // https://github.com/vlovgr/ciris/releases
  protected val cirisDep = "is.cir" %% "ciris" % "2.2.1"

  // https://mvnrepository.com/artifact/commons-codec/commons-codec
  protected val apacheCommonCodecDep =
    "commons-codec" % "commons-codec" % "1.15" withSources ()

  // https://mvnrepository.com/artifact/ch.qos.logback/logback-classic
  protected val logbackDep = "ch.qos.logback" % "logback-classic" % "1.2.7" withSources ()

  // https://mvnrepository.com/artifact/org.tpolecat/doobie-core
  protected val doobieCoreDep = "org.tpolecat" %% "doobie-core" % "1.0.0-RC1"

  // https://mvnrepository.com/artifact/org.tpolecat/doobie-postgres
  protected val doobiePostgresDep = "org.tpolecat" %% "doobie-postgres" % "1.0.0-RC1"

  // https://mvnrepository.com/artifact/org.tpolecat/doobie-hikari
  protected val doobieHikariDep = "org.tpolecat" %% "doobie-hikari" % "1.0.0-RC1"

}

object Libraries extends Libraries {

  val scalaMainVersion = "2.13.7"

  lazy val cats = Seq(
    catsDep,
    catsEffectDep
  )

  lazy val fs2 = Seq(
    fs2CoreDep,
    fs2IODep
  )

  lazy val log4cats = Seq(
    logbackDep,
    log4catsCoreDep,
    log4catsSlf4jDep
  )

  lazy val circe = Seq(
    circeCoreDep,
    circeParserDep,
    circeGenericDep
  )

  lazy val http4s = Seq(
    http4sDSLDep,
    http4sCirceDep,
    htt4psBlazeServerDep,
    http4sJdkHttpClientDep
  )

  lazy val ciris = Seq(cirisDep)

  lazy val crypt = Seq(apacheCommonCodecDep)

  lazy val doobie = Seq(doobieCoreDep, doobiePostgresDep, doobieHikariDep)

}
