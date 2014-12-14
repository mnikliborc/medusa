import sbt._
import Keys._

import BuildSettings._

object MedusaBuild extends Build {
  lazy val root = Project(
    id = "medusa",
    base = file("."),
    settings =
      Seq(
        libraryDependencies <+= (scalaVersion)("org.scala-lang" % "scala-compiler" % _),
        target := file("target")
      )
  ) aggregate(macros, test)

  lazy val test = Project(
    id = "medusa-test",
    base = file("test"),
    settings = buildSettings ++ Seq(libraryDependencies ++= Seq(
      "org.specs2" %% "specs2-core" % "2.4.14" % "test",
      "com.twitter" % "util-eval_2.10" % "6.22.1"))
  ) dependsOn(macros)

  lazy val macros = Project(
    id = "medusa-macros",
    base = file("macros"),
    settings = macroProjectSettings
  )
}

object BuildSettings {
  val paradiseVersion = "2.0.1"
  val paradiseDependency =
    "org.scalamacros" % "paradise" % paradiseVersion cross CrossVersion.full

  val buildSettings = Defaults.defaultSettings ++ Seq(
    version := "0.0.0-SNAPSHOT",
    scalaVersion := "2.11.4",
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-unchecked"
    ),
    resolvers ++= Seq(
      Resolver.sonatypeRepo("snapshots"),
      Resolver.sonatypeRepo("releases")
    ),

    /** We need the Macro Paradise plugin both to support the macro
      * annotations used in the public type provider implementation and to
      * allow us to use quasiquotes in both implementations. The anonymous
      * type providers could easily (although much less concisely) be
      * implemented without the plugin.
      */
    addCompilerPlugin(paradiseDependency)
  )
  
  val macroProjectSettings = buildSettings ++ Seq(
    libraryDependencies <+= (scalaVersion)(
      "org.scala-lang" % "scala-reflect" % _
    ),
    libraryDependencies ++= (
      /*if (scalaVersion.value.startsWith("2.11"))*/ List(paradiseDependency) //else Nil
    )
  )
}
