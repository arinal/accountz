import Dependencies._

ThisBuild / scalaVersion := "2.13.7"
ThisBuild / version := "0.1.0"
ThisBuild / organization := "io.lamedh"
ThisBuild / organizationName := "lamedh"

lazy val common = (project in file("modules/common"))
  .settings(
    name := "common",
    libraryDependencies ++= layer.core
  )

lazy val core = (project in file("modules/accountz.core"))
  .settings(
    name := "accountz-core",
    libraryDependencies ++= layer.core
  )
  .dependsOn(common)

lazy val infraRepoDoobie = (project in file("modules/accountz.infra.doobie"))
  .settings(
    name := "accountz-infra-doobie",
    libraryDependencies ++= layer.doobie
  )
  .dependsOn(core)

lazy val infraRepoMemory = (project in file("modules/accountz.infra.inmemory"))
  .settings(name := "accountz-infra-memory")
  .dependsOn(core)

lazy val infraUserClient =
  (project in file("modules/accountz.infra.userclient"))
    .settings(name := "accountz-infra-userclient")
    .dependsOn(core)

lazy val appRest = (project in file("modules/accountz.app.rest"))
  .settings(
    name := "accountz-app-rest",
    libraryDependencies ++= layer.rest
  )
  .dependsOn(core)

lazy val appKonsole = (project in file("modules/accountz.app.konsole"))
  .settings(name := "accountz-app-konsole")
  .dependsOn(core)

lazy val boot = (project in file("modules/accountz.boot"))
  .settings(
    name := "accountz-boot",
    libraryDependencies ++= layer.boot
  )
  .dependsOn(
    core,
    infraRepoDoobie,
    infraRepoMemory,
    infraUserClient,
    appRest,
    appKonsole
  )
