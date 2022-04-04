import sbt._

object Dependencies {

  object Version {
    val zio            = "1.0.13"
    val zioInteropCats = "3.2.9.0"
    val doobie         = "1.0.0-RC2"
    val pureconfig     = "0.17.1"
    val flywayDb       = "8.4.3"
  }

  val flywayDb   = "org.flywaydb"          %  "flyway-core" % Version.flywayDb
  val pureconfig = "com.github.pureconfig" %% "pureconfig"  % Version.pureconfig

  object Cats {
    val cats = "org.typelevel" %% "cats-core" % "2.7.0"
  }

  object Http4s {
    val blazeServer = "org.http4s" %% "http4s-blaze-server" % "1.0.0-M32"
    val dsl         = "org.http4s" %% "http4s-dsl"          % "1.0.0-M32"
  }

  object Zio {
    val zio         = "dev.zio" %% "zio" % Version.zio
    val interopCats = "dev.zio" %% "zio-interop-cats" % Version.zioInteropCats
    // val test        = "dev.zio" %% "zio-test" % zioVersion % "test"
    // val testsbt     = "dev.zio" %% "zio-test-sbt" % zioVersion % "test"
    // val zioLogging = "dev.zio" %% "zio-logging" % zioLoggingVersion
    // val zioLoggingSlf4j = "dev.zio" %% "zio-logging-slf4j" % zioLoggingVersion
    // val log4jAPI = "org.apache.logging.log4j" % "log4j-api" % log4j2Version
    // val log4jCore = "org.apache.logging.log4j" % "log4j-core" % log4j2Version
    // val log4jSlf4jImpl = "org.apache.logging.log4j" % "log4j-slf4j-impl" % log4j2Version
  }

  object Doobie {
    val core     = "org.tpolecat" %% "doobie-core"     % Version.doobie
    val h2       = "org.tpolecat" %% "doobie-h2"       % Version.doobie
    val hikari   = "org.tpolecat" %% "doobie-hikari"   % Version.doobie
    val postgres = "org.tpolecat" %% "doobie-postgres" % Version.doobie
  }

  object layer {
    val core   = Seq(Cats.cats, Zio.zio, Zio.interopCats)
    val doobie = Seq(Doobie.core, Doobie.h2, Doobie.hikari, Doobie.postgres)
    val rest   = Seq(Http4s.blazeServer, Http4s.dsl)
    val boot   = Seq(pureconfig)
  }
}
