val scala3Version = "3.8.4"
val PekkoVersion = "1.6.0"

ThisBuild / scalaVersion := scala3Version
ThisBuild / organization := "io.github.nicolasfara"
ThisBuild / homepage := Some(
  url(
    "https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects"
  )
)
ThisBuild / licenses := List(
  "Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0")
)
ThisBuild / versionScheme := Some("early-semver")
ThisBuild / developers := List(
  Developer(
    "nicolasfara",
    "Nicolas Farabegoli",
    "nicolas.farabegoli@gmail.com",
    url("https://nicolasfarabegoli.it")
  )
)
ThisBuild / scalacOptions ++= Seq(
  "-Werror",
  "-Wunused:all",
  "-Wvalue-discard",
  "-Wnonunit-statement",
  "-Yexplicit-nulls",
  "-Wsafe-init",
  "-Ycheck-reentrant",
  "-Xcheck-macros",
  "-rewrite",
  "-indent",
  "-unchecked",
  "-explain",
  "-feature",
  // "-language:strictEquality",
  "-language:implicitConversions"
)
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision
// ThisBuild / wartremoverErrors ++= Warts.allBut(Wart.Any, Wart.Throw, Wart.Recursion, Wart.Var, Wart.ImplicitParameter, Wart.Nothing)
ThisBuild / libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.20" % Test,
  "org.apache.pekko" %% "pekko-actor-typed" % PekkoVersion,
  "org.apache.pekko" %% "pekko-actor-testkit-typed" % PekkoVersion % Test,
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.6",
  "ch.qos.logback" % "logback-classic" % "1.5.35"
)

lazy val root = project
  .in(file("."))
  .settings(
    name := "Template-for-Scala-Multiplatform-Projects",
  )

lazy val basics = project
  .in(file("basics"))
  .settings(
    name := "basics",
  )
  .dependsOn(root)

lazy val cluster = project
  .in(file("cluster"))
  .settings(
    name := "cluster",
    run / fork := true,
    assembly / assemblyOutputPath := baseDirectory.value / "target" / "cluster.jar",
    libraryDependencies ++= Seq(
      "org.apache.pekko" %% "pekko-cluster-typed" % PekkoVersion,
      "org.apache.pekko" %% "pekko-cluster-sharding-typed" % PekkoVersion,
      "org.apache.pekko" %% "pekko-serialization-jackson" % PekkoVersion
    ),
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", "versions", "9", "module-info.class") => MergeStrategy.discard
      case PathList("module-info.class")                              => MergeStrategy.discard
      case x =>
        val oldStrategy = (assembly / assemblyMergeStrategy).value
        oldStrategy(x)
    }
  )
  .dependsOn(root)
