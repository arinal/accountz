package io.lamedh.accountz.boot

import io.lamedh.accountz.app.konsole
import io.lamedh.accountz.app.rest.HttpConfig
import io.lamedh.accountz.infra.repo.doobie.DBConfig
import pureconfig.{ConfigConvert, ConfigSource}
import pureconfig.generic.auto._
import zio._

package object config {

  final case class Config(http: HttpConfig, db: DBConfig)

  val layer = {
    val rootConfig = ZLayer.fromEffect {
      ZIO
        .fromEither(ConfigSource.default.load[Config])
        .orDieWith(err =>
          new IllegalStateException(s"Error loading configuration: $err")
        )
    }
    val dbConfig = ZLayer.fromService[Config, DBConfig](_.db)
    val httpConfig = ZLayer.fromService[Config, HttpConfig](_.http)

    rootConfig >>> (dbConfig ++ httpConfig)
  }
}
