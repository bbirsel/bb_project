ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

lazy val root = (project in file("."))
  .settings(
    name := "scala_project_new",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % "2.6.20",
      "com.typesafe.akka" %% "akka-stream" % "2.6.20",
      "com.typesafe.akka" %% "akka-http" % "10.2.7",
      "com.typesafe.akka" %% "akka-http-spray-json" % "10.2.7",
      "ch.qos.logback" % "logback-classic" % "1.2.11",
      "org.apache.kafka" % "kafka-clients" % "3.7.0",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4",
  ))
