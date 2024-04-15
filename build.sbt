val scalav = "3.4.1"
val zio = "2.0.22"
val json = "0.6.2"

lazy val app = project.in(file(".")).settings(
  scalaVersion := scalav
, libraryDependencies ++= Seq(
    "dev.zio" %% "zio-test-sbt" % zio % Test
  )
, testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
, scalacOptions ++= scalacOptionsCommon
, Compile / mainClass := Some("cli.App")
, Test / fork := true
).dependsOn(cli).aggregate(service, cli)

lazy val service = project.in(file("service")).settings(
  scalaVersion := scalav
, Compile / scalaSource := baseDirectory.value / "src"
, libraryDependencies ++= Seq(
    "dev.zio" %% "zio" % zio
  , "dev.zio" %% "zio-json" % json
  )
, scalacOptions ++= scalacOptionsCommon
)

lazy val cli = project.in(file("cli")).settings(
  scalaVersion := scalav
, Compile / scalaSource := baseDirectory.value / "src"
, scalacOptions ++= scalacOptionsCommon
, run / fork := true
, docker / dockerfile := {
    val appDir: File = stage.value
    val targetDir = "/app"
    new Dockerfile {
      from("eclipse-temurin:22_36-jre")
      entryPoint(s"$targetDir/bin/${executableScriptName.value}")
      copy(appDir, targetDir, chown = "daemon:daemon")
    }
  }
).dependsOn(service).enablePlugins(sbtdocker.DockerPlugin, JavaAppPackaging)

val scalacOptionsCommon = Seq(
  "-language:strictEquality"
, "-source", "future"
, "-Wunused:imports"
, "-Yexplicit-nulls"
)

Global / onChangedBuildSource := ReloadOnSourceChanges
