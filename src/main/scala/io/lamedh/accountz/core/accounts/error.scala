package io.lamedh.accountz.core.accounts

import cats.data.NonEmptyChain

trait AccountError {
  def message: NonEmptyChain[String]
}

case class AlreadyExistingAccount(no: String) extends AccountError {
  val message = NonEmptyChain(s"Already existing account with no $no")
}

case class NonExistingAccount(no: String) extends AccountError {
  val message = NonEmptyChain(s"No existing account with no $no")
}

case class ClosedAccount(no: String) extends AccountError {
  val message = NonEmptyChain(s"Account with no $no is closed")
}

case object RateMissingForSavingsAccount extends AccountError {
  val message = NonEmptyChain("Rate needs to be given for savings account")
}

case class EtcError(msgs: NonEmptyChain[String]) extends AccountError {
  val message = msgs
}
