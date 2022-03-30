package io.lamedh.accountz.core.accounts

import zio._
import java.util.Date

trait AccountRepository {
  def query(no: String): Task[Option[Account]]
  def store(a: Account): Task[Account]
  def query(openedOn: Date): Task[Seq[Account]]
  def all: Task[Seq[Account]]
  def balance(no: String): Task[Option[Balance]]
}

object AccountRepository {

  def query(no: String): RIO[Has[AccountRepository], Option[Account]] =
    ZIO.serviceWith(_.query(no))

  def all: RIO[Has[AccountRepository], Seq[Account]] =
    ZIO.serviceWith(_.all)

  def store(a: Account): RIO[Has[AccountRepository], Account] =
    ZIO.serviceWith(_.store(a))

  def query(openedOn: Date): RIO[Has[AccountRepository], Seq[Account]] =
    ZIO.serviceWith(_.query(openedOn))

  def balance(no: String): RIO[Has[AccountRepository], Option[Balance]] =
    ZIO.serviceWith(_.balance(no))
}
