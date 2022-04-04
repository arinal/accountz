package io.lamedh.accountz.core.accounts

import io.lamedh.common._
import zio._

import java.util.Date

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
