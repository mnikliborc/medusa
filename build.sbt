name := "medusa"

version := "1.0"

scalaVersion in ThisBuild := "2.11.4"

libraryDependencies in ThisBuild <+= (scalaVersion)("org.scala-lang" % "scala-compiler" % _)

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-Xlint",
  "-feature",
  "-language:_",
  "-Ymacro-debug-lite"
)

autoCompilerPlugins := true

resolvers += Resolver.sonatypeRepo("releases")

addCompilerPlugin("org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full)

libraryDependencies in ThisBuild <+= scalaVersion("org.scala-lang" % "scala-reflect" % _)

mainClass in (Compile,run) := Some("me.niklim.medusa.App")
