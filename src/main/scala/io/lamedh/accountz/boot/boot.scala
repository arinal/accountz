package io.lamedh.accountz.boot

import io.lamedh.accountz.app.konsole
import io.lamedh.accountz.app.rest
import io.lamedh.accountz.app.rest.HttpConfig
import io.lamedh.accountz.infra.repo.inmemory.InMemoryAccountRepository
import io.lamedh.accountz.infra.repo.doobie.DoobieAccountRepository
import io.lamedh.accountz.core.accounts.AccountLive
import io.lamedh.accountz.core.accounts.AccountRepository
import io.lamedh.accountz.infra.repo.doobie.DBConfig
import zio._
import zio.blocking.Blocking

object Boot extends App {

  val layer =
    Blocking.live >+>
      config.layer >+>
      InMemoryAccountRepository.layer >+>
      // DoobieAccountRepository.layer.orDie >+>
      AccountLive.layer

  override def run(args: List[String]) = {
    val app =
      // konsole.Main.run
      rest.Main.run
    app.provideCustomLayer(layer)
  }
}
