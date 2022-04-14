package io.lamedh.accountz
package infra.userclient

import core.users._
import io.lamedh.common._
import zio._

import java.util.UUID

class UserHttpClient extends UserAlg {
  def get(uuid: UUID): IO[UserError, User] =
    ZIO.succeed(User(uuid, s"user-$uuid", today()))
}

object UserHttpClient {
  val layer: ZLayer[Any, Nothing, Has[UserAlg]] =
    ZLayer.succeed(new UserHttpClient)
}
