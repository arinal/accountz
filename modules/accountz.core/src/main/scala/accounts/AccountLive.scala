package io.lamedh.accountz.core.accounts

import io.lamedh.common._

import zio._

import cats.data.NonEmptyChain
import java.util.Date

object AccountLive {
  val layer: ZLayer[Has[AccountRepository], Nothing, Has[AccountAlg]] =
    ZLayer.fromService[AccountRepository, AccountAlg](new AccountLive(_))
}

class AccountLive(repo: AccountRepository) extends AccountAlg {

  def open(
      no: String,
      name: String,
      rate: Option[BigDecimal],
      openingDate: Option[Date],
      accountType: AccountType
  ): IO[AccountError, Account] =
    withError(repo.query(no)).flatMap(maybeAccount =>
      doOpenAccount(
        maybeAccount,
        no,
        name,
        rate,
        openingDate,
        accountType
      )
    )

  def close(
      no: String,
      closeDate: Option[Date]
  ): IO[AccountError, Account] =
    withError(repo.query(no)).flatMap {
      case Some(a) =>
        createOrUpdate(Account.close(a, closeDate.getOrElse(today())))
      case None => IO.fail(NonExistingAccount(no))
    }

  def debit(no: String, amount: Amount): IO[AccountError, Account] =
    update(no, amount, D)
  def credit(no: String, amount: Amount): IO[AccountError, Account] =
    update(no, amount, C)

  def balance(no: String): IO[AccountError, Balance] =
    withError(repo.query(no)).flatMap {
      case Some(a) => IO.succeed(a.balance)
      case None    => IO.fail(NonExistingAccount(no))
    }

  def balanceByAccount: IO[AccountError, Seq[(String, Amount)]] =
    withError(repo.all.map { accounts =>
      accounts.map(a => (a.no, a.balance.amount))
    })

  def transfer(
      from: String,
      to: String,
      amount: Amount
  ): IO[AccountError, (Account, Account)] = for {
    a <- debit(from, amount)
    b <- credit(to, amount)
  } yield ((a, b))

  private trait DC
  private case object D extends DC
  private case object C extends DC

  private def doOpenAccount(
      maybeAccount: Option[Account],
      no: String,
      name: String,
      rate: Option[BigDecimal],
      openingDate: Option[Date],
      accountType: AccountType
  ): IO[AccountError, Account] =
    maybeAccount
      .map(_ => IO.fail(AlreadyExistingAccount(no)))
      .getOrElse(createOrUpdate(no, name, rate, openingDate, accountType))

  private def createOrUpdate(
      no: String,
      name: String,
      rate: Option[BigDecimal],
      openingDate: Option[Date],
      accountType: AccountType
  ): IO[AccountError, Account] = accountType match {

    case Checking =>
      createOrUpdate(
        Account.checkingAccount(no, name, openingDate, None, Balance())
      )
    case Savings =>
      rate
        .map(r =>
          createOrUpdate(
            Account.savingsAccount(no, name, r, openingDate, None, Balance())
          )
        )
        .getOrElse(IO.fail(RateMissingForSavingsAccount))
  }

  private def createOrUpdate(
      errorOrAccount: ErrorOr[Account]
  ): IO[AccountError, Account] = errorOrAccount match {
    case Left(errs) => IO.fail(EtcError(errs))
    case Right(a)   => withError(repo.store(a))
  }

  private def update(
      no: String,
      amount: Amount,
      debitCredit: DC
  ): IO[AccountError, Account] = {
    val multiplier = if (debitCredit == D) (-1) else 1

    withError(repo.query(no)).flatMap {
      case Some(a) =>
        createOrUpdate(Account.updateBalance(a, multiplier * amount))
      case None => IO.fail(NonExistingAccount(no))
    }
  }

  private def withError[A](t: Task[A]): IO[AccountError, A] =
    t.foldM(
      error => IO.fail(EtcError(NonEmptyChain(error.getMessage))),
      success => IO.succeed(success)
    )
}
