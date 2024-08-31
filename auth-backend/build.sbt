name := """auth-backend"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)
scalafmtOnCompile := true
scalafmtConfig := file(".scalafmt.conf")

// テストコードもフォーマットするように設定
(Test / scalafmt) := (scalafmt in Test).value

scalaVersion := "2.13.14"
val AkkaVersion = "2.6.21"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.1" % Test

libraryDependencies ++= Seq(
    "com.typesafe.play" %% "play-slick" % "5.3.0",
    "com.typesafe.play" %% "play-slick-evolutions" % "5.3.0",
    "mysql" % "mysql-connector-java" % "8.0.33",
    "org.mindrot" % "jbcrypt" % "0.4",
    "com.github.jwt-scala" %% "jwt-play" % "10.0.1",
    "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.1" % Test,
    "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
    "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
    "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test
)