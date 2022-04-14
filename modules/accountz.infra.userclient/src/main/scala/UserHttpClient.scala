package io.lamedh.accountz.infra.userclient

import java.util.UUID

class UserHttpClient extends UserAlg {
  def get(uuid: UUID): IO[UserError, User] = ???
}
