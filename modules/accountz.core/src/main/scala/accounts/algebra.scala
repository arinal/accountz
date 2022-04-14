package io.lamedh.accountz
package core.accounts

import core.users.UserAlg
import io.lamedh.common._
import zio._

import java.util.Date
import java.util.UUID

sealed trait AccountType
case object Checking extends AccountType
case object Savings extends AccountType

trait AccountAlg {

  def open(
      no: String,
      name: String,
      rate: Option[BigDecimal],
      openingDate: Option[Date],
      accountType: AccountType
  ): IO[AccountError, Account]

  def open(
      no: String,
      userId: UUID,
      rate: Option[BigDecimal],
      openingDate: Option[Date],
      accountType: AccountType
  ): ZIO[Has[UserAlg], AccountError, Account]

  def close(
      no: String,
      closeDate: Option[Date]
  ): IO[AccountError, Account]

  def debit(no: String, amount: Amount): IO[AccountError, Account]
  def credit(no: String, amount: Amount): IO[AccountError, Account]
  def balance(no: String): IO[AccountError, Balance]

  def transfer(
      from: String,
      to: String,
      amount: Amount
  ): IO[AccountError, (Account, Account)]

  def balanceByAccount: IO[AccountError, Seq[(String, Amount)]]
}

object AccountAlg {

  def open(
      no: String,
      name: String,
      rate: Option[BigDecimal],
      openingDate: Option[Date],
      accountType: AccountType
  ): ZIO[Has[AccountAlg], AccountError, Account] =
    ZIO.serviceWith(_.open(no, name, rate, openingDate, accountType))

  def open(
      no: String,
      userId: UUID,
      rate: Option[BigDecimal],
      openingDate: Option[Date],
      accountType: AccountType
  ): ZIO[Has[UserAlg] with Has[AccountAlg], AccountError, Account] =
    for {
      acc <- ZIO.accessM[Has[AccountAlg] with Has[UserAlg]](
        _.get.open(no, userId, rate, openingDate, accountType)
      )
    } yield acc

  def close(
      no: String,
      closeDate: Option[Date]
  ): ZIO[Has[AccountAlg], AccountError, Account] =
    ZIO.serviceWith(_.close(no, closeDate))

  def debit(
      no: String,
      amount: Amount
  ): ZIO[Has[AccountAlg], AccountError, Account] =
    ZIO.serviceWith(_.debit(no, amount))

  def credit(
      no: String,
      amount: Amount
  ): ZIO[Has[AccountAlg], AccountError, Account] =
    ZIO.serviceWith(_.credit(no, amount))

  def balance(
      no: String
  ): ZIO[Has[AccountAlg], AccountError, Balance] =
    ZIO.serviceWith(_.balance(no))

  def transfer(
      from: String,
      to: String,
      amount: Amount
  ): ZIO[Has[AccountAlg], AccountError, (Account, Account)] =
    ZIO.serviceWith(_.transfer(from, to, amount))

  def balanceByAccount
      : ZIO[Has[AccountAlg], AccountError, Seq[(String, Amount)]] =
    ZIO.serviceWith(_.balanceByAccount)
}
