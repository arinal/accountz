package io.lamedh.accountz
package infra.repo.doobie

import core.accounts._
import io.lamedh.common._

import doobie._
import doobie.free.connection
import doobie.hikari._
import doobie.implicits._
import doobie.implicits.javasql._
import doobie.util.transactor.Transactor

import zio._
import zio.blocking.Blocking
import zio.interop.catz._

import java.util.Date

final class DoobieAccountRepository(xa: Transactor[Task])
    extends AccountRepository {
  import DoobieAccountRepository.SQL

  def all: Task[Seq[Account]] =
    SQL.getAll
      .to[List]
      .transact(xa)
      .orDie

  def query(no: String): Task[Option[Account]] =
    SQL
      .get(no)
      .option
      .transact(xa)
      .orDie

  def store(a: Account): Task[Account] =
    SQL
      .upsert(a)
      .run
      .transact(xa)
      .map(_ => a)
      .orDie

  def query(openedOn: Date): Task[Seq[Account]] =
    SQL
      .getByDateOfOpen(openedOn)
      .to[List]
      .transact(xa)
      .orDie

  def balance(no: String): Task[Option[Balance]] =
    query(no).map(_.map(_.balance))
}

object DoobieAccountRepository {

  def layer: ZLayer[Blocking with Has[DBConfig], Throwable, Has[
    AccountRepository
  ]] = {
    import zio.interop.catz.implicits._

    implicit val zioRuntime: zio.Runtime[zio.ZEnv] = zio.Runtime.default

    implicit val dispatcher: cats.effect.std.Dispatcher[zio.Task] =
      zioRuntime
        .unsafeRun(
          cats.effect.std
            .Dispatcher[zio.Task]
            .allocated
        )
        ._1

    def mkTransactor(
        cfg: DBConfig
    ): ZManaged[Blocking with Has[DBConfig], Throwable, HikariTransactor[
      Task
    ]] =
      for {
        rt <- ZIO.runtime[Any].toManaged_
        xa <-
          HikariTransactor
            .newHikariTransactor[Task](
              cfg.driver,
              cfg.url,
              cfg.user,
              cfg.password,
              rt.platform.executor.asEC
            )
            .toManaged
      } yield xa

    ZLayer.fromManaged {
      for {
        cfg <- ZIO.access[Has[DBConfig]](_.get).toManaged_
        transactor <- mkTransactor(cfg)
      } yield (new DoobieAccountRepository(transactor): AccountRepository)
    }
  }

  object SQL {
    def upsert(account: Account): Update0 = {
      // upsert in h2
      sql"""
        merge into accounts key (no)
        values (${account.no}, ${account.name}, null, ${account.dateOfOpen
        .getOrElse(today())}, ${account.dateOfClose}, ${account.balance.amount})
      """.update
    }

    implicit val accountRead: Read[Account] =
      Read[(String, String, Option[BigDecimal], Date, Option[Date], BigDecimal)]
        .map { case (no, nm, rate, openDt, closeDt, bal) =>
          rate match {
            case Some(r) =>
              Account.savingsAccount(
                no,
                nm,
                r,
                Some(openDt),
                closeDt,
                Balance(bal)
              ) match {
                case Right(a) => a
                case Left(_)  => ???
              }
            case None =>
              Account.checkingAccount(
                no,
                nm,
                Some(openDt),
                closeDt,
                Balance(bal)
              ) match {
                case Right(a) => a
                case Left(_)  => ???
              }
          }
        }

    def get(no: String): Query0[Account] = sql"""
      select * from accounts where no = $no
      """.query[Account]

    def getAll: Query0[Account] = sql"""
      select * from accounts
      """.query[Account]

    def getByDateOfOpen(openDate: Date): Query0[Account] = sql"""
      select * from accounts where dateOfOpen = $openDate
      """.query[Account]
  }
}
