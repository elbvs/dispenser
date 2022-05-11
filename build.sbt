
ThisBuild / organization := "ru.bvs"
ThisBuild / version := "1.0.0"

ThisBuild / scalaVersion := "2.13.8"

ThisBuild / libraryDependencySchemes +=
  "org.scala-lang.modules" %% "scala-java8-compat" % VersionScheme.Always

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.3" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.1.1" % Test
val cats = "org.typelevel" %% "cats-effect" % "2.5.3"
val time = "com.github.nscala-time" %% "nscala-time" % "2.30.0"

lazy val `dispenser` = (project in file("."))
  .aggregate(`dispenser-api`, `dispenser-impl`)

lazy val `dispenser-api` = (project in file("dispenser-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `dispenser-impl` = (project in file("dispenser-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      cats,
      time,
      macwire,
      scalaTest
    )
  )
  .settings(lagomForkedTestSettings)
  .dependsOn(`dispenser-api`)
