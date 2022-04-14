package io.lamedh.accountz.core.users

import java.util.UUID
import java.util.Date

case class User(id: UUID, username: String, registerDate: Date)

trait UserError {
  def message: String
}

case class UserNotFound(id: UUID) extends UserError {
  def message = s"User $id not found"
}
case class GenericError(message: String) extends UserError
