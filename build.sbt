import com.softwaremill.SbtSoftwareMillCommon.commonSmlBuildSettings

lazy val commonSettings = commonSmlBuildSettings ++ Seq(
  organization := "com.softwaremill.demo",
  scalaVersion := "3.3.4"
)

val scalaTest = "org.scalatest" %% "scalatest" % "3.2.19" % Test

lazy val rootProject = (project in file("."))
  .settings(commonSettings: _*)
  .settings(publishArtifact := false, name := "flow-demo")
  .aggregate(core)

lazy val core: Project = (project in file("core"))
  .settings(commonSettings: _*)
  .settings(
    name := "core",
    libraryDependencies ++= Seq(
      "com.softwaremill.ox" %% "core" % "0.5.2",
      "com.softwaremill.ox" %% "kafka" % "0.5.2",
      "com.softwaremill.sttp.tapir" %% "tapir-netty-server-sync" % "1.11.7",
      "com.softwaremill.sttp.client4" %% "core" % "4.0.0-M19",
      "ch.qos.logback" % "logback-classic" % "1.5.11",
      "io.lettuce" % "lettuce-core" % "6.4.0.RELEASE",
      scalaTest
    )
  )
