package io.lamedh.accountz
package app.rest

import core.accounts.AccountAlg
import core.accounts.Checking
import core.accounts.Savings

import zio._
import zio.console._
import zio.interop.catz._

import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.blaze.BlazeServerBuilder

case class HttpConfig(host: String, port: Int)

object Main {

  val dsl = Http4sDsl[Task]
  import dsl._

  def routes(service: AccountAlg) = HttpRoutes
    .of[Task] { case GET -> Root =>
      service.balanceByAccount
        .mapError(err => new Exception)
        .orDie
        .flatMap(b => Ok(b.toString))
    }
    .orNotFound

  import AccountAlg._
  val openAccounts =
    for {
      _ <- open("a1234", "a1name", None, None, Checking)
      _ <- open("a2345", "a2name", None, None, Checking)
      _ <- open("a3456", "a3name", Some(BigDecimal(5.8)), None, Savings)
      _ <- open("a4567", "a4name", None, None, Checking)
      _ <- open("a5678", "a5name", Some(BigDecimal(2.3)), None, Savings)
    } yield ()

  val run = {
    openAccounts *>
      ZIO
        .runtime[ZEnv with Has[AccountAlg] with Has[HttpConfig]]
        .flatMap { implicit runtime =>
          val service = runtime.environment.get[AccountAlg]
          val config = runtime.environment.get[HttpConfig]
          BlazeServerBuilder[Task](runtime.platform.executor.asEC)
            .bindHttp(config.port, config.host)
            .withHttpApp(routes(service))
            .resource
            .toManagedZIO
            .useForever
        }
  }.foldCauseM(
    err => putStrLn(err.prettyPrint).orDie.as(ExitCode.failure),
    _ => ZIO.succeed(ExitCode.success)
  )
}
