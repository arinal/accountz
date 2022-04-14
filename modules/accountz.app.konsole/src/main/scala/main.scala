package io.lamedh.accountz
package app.konsole

import core.accounts.Checking
import core.accounts.Savings
import core.users.UserAlg
import io.lamedh.common._

import zio._
import zio.console._
import zio.blocking.Blocking
import java.util.UUID

object Main {

  import core.accounts.AccountAlg

  def run: URIO[Has[AccountAlg] with Has[UserAlg] with Console, ExitCode] = {
    import AccountAlg._
    val opens =
      for {
        _ <- open("a1234", "a1name", None, None, Checking)
        _ <- open("a2345", "a2name", None, None, Checking)
        _ <- open("a3456", "a3name", Some(BigDecimal(5.8)), None, Savings)
        _ <- open("a4567", "a4name", None, None, Checking)
        _ <- open("a5678", "a5name", Some(BigDecimal(2.3)), None, Savings)
        _ <- open("auser", UUID.randomUUID(), None, None, Checking)
      } yield ()

    val credits =
      for {
        _ <- credit("a1234", 1000)
        _ <- credit("a2345", 2000)
        _ <- credit("a3456", 3000)
        _ <- credit("a4567", 4000)
        _ <- credit("auser", 5000)
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
