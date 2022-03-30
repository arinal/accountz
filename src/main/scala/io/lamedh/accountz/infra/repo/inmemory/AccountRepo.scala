package io.lamedh.accountz.infra.repo.inmemory

import io.lamedh.common.today
import io.lamedh.accountz.core.accounts._
import zio._
import java.util.Date

class InMemoryAccountRepository(ref: Ref[Map[String, Account]])
    extends AccountRepository {

  override def all: Task[Seq[Account]] =
    ref.get.map(_.values.toList)

  override def query(no: String): Task[Option[Account]] =
    ref.get.map(_.get(no))

  override def query(openedOn: Date): Task[Seq[Account]] =
    ref.get.map(
      _.values.filter(_.dateOfOpen.getOrElse(today) == openedOn).toSeq
    )

  override def store(a: Account): Task[Account] =
    ref.update(m => m + (a.no -> a)).map(_ => a)

  override def balance(no: String): Task[Option[Balance]] =
    ref.get.map(_.get(no).map(_.balance))
}

object InMemoryAccountRepository {

  val layer: ZLayer[Any, Nothing, Has[AccountRepository]] =
    ZLayer.fromEffect {
      Ref
        .make(Map.empty[String, Account])
        .map(new InMemoryAccountRepository(_))
    }
}
