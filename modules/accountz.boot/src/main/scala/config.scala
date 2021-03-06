package io.lamedh.accountz
package boot

import app.konsole
import app.rest.HttpConfig
import infra.repo.doobie.DBConfig

import pureconfig.{ConfigConvert, ConfigSource}
import pureconfig.generic.auto._
import zio._

package object config {

  final case class Config(http: HttpConfig, db: DBConfig)

  val layer: ZLayer[Any, Nothing, Has[DBConfig] with Has[HttpConfig]] = {
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
