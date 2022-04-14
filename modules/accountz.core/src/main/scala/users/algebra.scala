package io.lamedh.accountz.core.users

import zio._
import java.util.UUID

trait UserAlg {
  def get(id: UUID): IO[UserError, User]
}

object UserAlg {
  def get(id: UUID): ZIO[Has[UserAlg], UserError, User] =
    ZIO.serviceWith[UserAlg](_.get(id))
}
