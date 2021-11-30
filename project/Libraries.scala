import sbt._

private object ProjectRepo {
  // https://mvnrepository.com/artifact/co.fs2/fs2-core
  val fs2CoreDep = "co.fs2" %% "fs2-core" % "3.2.2" withSources ()

  // https://mvnrepository.com/artifact/co.fs2/fs2-io
  val fs2IODep = "co.fs2" %% "fs2-io" % "3.2.2" withSources ()

  // https://mvnrepository.com/artifact/org.typelevel/cats-core
  val catsDep = "org.typelevel" %% "cats-core" % "2.6.1" withSources ()

  // https://mvnrepository.com/artifact/org.typelevel/cats-effect
  val catsEffectDep =
    "org.typelevel" %% "cats-effect" % "3.3-393-da7c7c7" withSources ()

  // https://mvnrepository.com/artifact/org.typelevel/log4cats-core
  val log4catsCoreDep =
    "org.typelevel" %% "log4cats-core" % "2.1.1" withSources ()

  // https://mvnrepository.com/artifact/org.typelevel/log4cats-slf4j
  val log4catsSlf4jDep =
    "org.typelevel" %% "log4cats-slf4j" % "2.1.1" withSources ()

  // https://mvnrepository.com/artifact/org.http4s/http4s-dsl
  val http4sDSLDep = "org.http4s" %% "http4s-dsl" % "1.0.0-M29"

  // https://mvnrepository.com/artifact/org.http4s/http4s-jdk-http-client
  val http4sJdkHttpClientDep =
    "org.http4s" %% "http4s-jdk-http-client" % "0.6.0-M6"

  // https://mvnrepository.com/artifact/org.http4s/http4s-blaze-server
  val htt4psBlazeServerDep =
    "org.http4s" %% "http4s-blaze-server" % "1.0.0-M29"

  // https://mvnrepository.com/artifact/org.http4s/http4s-circe
  val http4sCirceDep = "org.http4s" %% "http4s-circe" % "1.0.0-M29"

  // https://mvnrepository.com/artifact/io.circe/circe-core
  val circeCoreDep = "io.circe" %% "circe-core" % "0.14.1" withSources ()

  // https://mvnrepository.com/artifact/io.circe/circe-parser
  val circeParserDep = "io.circe" %% "circe-parser" % "0.14.1" withSources ()

  // https://mvnrepository.com/artifact/io.circe/circe-generic
  val circeGenericDep =
    "io.circe" %% "circe-generic" % "0.14.1" withSources ()

  // https://github.com/vlovgr/ciris/releases
  val cirisDep = "is.cir" %% "ciris" % "2.2.1"

  // https://mvnrepository.com/artifact/commons-codec/commons-codec
  val apacheCommonCodecDep =
    "commons-codec" % "commons-codec" % "1.15" withSources ()

  // https://mvnrepository.com/artifact/ch.qos.logback/logback-classic
  val logbackDep = "ch.qos.logback" % "logback-classic" % "1.2.7" withSources ()

}

object Libraries {

  import ProjectRepo._

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

}
