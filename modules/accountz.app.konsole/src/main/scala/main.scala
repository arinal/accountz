package io.lamedh.accountz.app.konsole

import io.lamedh.accountz.core.accounts.Checking
import io.lamedh.accountz.core.accounts.Savings
import io.lamedh.common._

import zio._
import zio.console._
import zio.blocking.Blocking

object Main {

  import io.lamedh.accountz.core.accounts.AccountAlg
  import AccountAlg._

  def run: URIO[Has[AccountAlg] with Console, ExitCode] = {
    val opens =
      for {
        _ <- open("a1234", "a1name", None, None, Checking)
        _ <- open("a2345", "a2name", None, None, Checking)
        _ <- open("a3456", "a3name", Some(BigDecimal(5.8)), None, Savings)
        _ <- open("a4567", "a4name", None, None, Checking)
        _ <- open("a5678", "a5name", Some(BigDecimal(2.3)), None, Savings)
      } yield ()

    val credits =
      for {
        _ <- credit("a1234", 1000)
        _ <- credit("a2345", 2000)
        _ <- credit("a3456", 3000)
        _ <- credit("a4567", 4000)
      } yield ()

    val app = for {
      _ <- opens
      _ <- credits
      a <- balanceByAccount
    } yield a

    app
      .flatMap(a => putStrLn(a.toString))
      .orDieWith(e => new Exception(e.toString))
      .as(ExitCode.success)
  }
}
