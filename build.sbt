import sbt.Keys.scalaVersion
import sbt._

name := "BruteFaction"
scalaVersion := Libraries.scalaMainVersion

// ************************************************
// ************************************************
// Common Settings
// ************************************************
// ************************************************
lazy val commonSettings = {
  import Libraries._
  Seq(
    organizationName := "RAP",
    version := "0.0.1",
    scalaVersion := Libraries.scalaMainVersion,
    scalacOptions ++= CompilerSettings.compilerFlags,
    discoveredMainClasses := Seq.empty,
    libraryDependencies ++= cats ++ fs2 ++ ciris ++ circe ++ crypt ++ http4s ++ log4cats,
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
    addCompilerPlugin(
      ("org.typelevel" %% "kind-projector" % "0.13.2").cross(CrossVersion.full)
    )
  )
}

// ************************************************
// ************************************************
// Projects
// ************************************************
// ************************************************
lazy val root = project
  .in(file("."))
  .settings(
    name := "brute-faction",
    scalaVersion := Libraries.scalaMainVersion
  )
  .aggregate(
    factionApp,
    factionRoutes,
    factionAlgebra,
    mercenaryApp,
    model,
    config
  )

// ************************************************
// Faction
// ************************************************
lazy val factionApp = project
  .in(file("./modules/app/faction-app"))
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(DockerPlugin)
  .settings(
    name := "faction-app",
    mainClass := Option("upt.ssc.bf.app.faction.Faction"),
    dockerExposedPorts := Seq(31513),
    dockerBaseImage := "openjdk:16-jdk",
    commonSettings
  )
  .dependsOn(model, config, factionRoutes)

lazy val factionRoutes = project
  .in(file("./modules/routes/faction-routes"))
  .settings(
    name := "faction-routes",
    commonSettings
  )
  .dependsOn(model, config, factionAlgebra)

lazy val factionAlgebra = project
  .in(file("./modules/algebra/faction-algebra"))
  .settings(
    name := "faction-algebra",
    commonSettings
  )
  .dependsOn(model, config)

// ************************************************
// Mercenary
// ************************************************
lazy val mercenaryApp = project
  .in(file("./modules/app/mercenary-app"))
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(DockerPlugin)
  .settings(
    name := "mercenary-app",
    mainClass := Option("upt.ssc.bf.app.mercenary.Mercenary"),
    dockerBaseImage := "openjdk:16-jdk",
    commonSettings
  )
  .dependsOn(model, config)

// ************************************************
// Common
// ************************************************
lazy val config = project
  .in(file("./modules/core/config"))
  .settings(
    name := "config",
    commonSettings
  )
  .dependsOn(model)

lazy val model = project
  .in(file("./modules/core/model"))
  .settings(
    name := "model",
    commonSettings
  )
