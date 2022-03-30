import Dependencies._

ThisBuild / scalaVersion     := "2.13.7"
ThisBuild / version          := "0.1.0"
ThisBuild / organization     := "io.lamedh"
ThisBuild / organizationName := "lamedh"

lazy val root = (project in file("."))
  .settings(
    name := "accountz",
    libraryDependencies ++= dependencies
  )
