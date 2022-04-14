package io.lamedh.accountz
package boot

import app.konsole
import app.rest
import app.rest.HttpConfig
import infra.repo.doobie.DBConfig
import infra.repo.inmemory.InMemoryAccountRepository
import infra.repo.doobie.DoobieAccountRepository
import infra.userclient.UserHttpClient
import core.accounts.AccountLive
import core.accounts.AccountRepository

import zio._
import zio.blocking.Blocking

object Boot extends App {

  val layer =
    Blocking.live >+>
      config.layer >+>
      InMemoryAccountRepository.layer >+>
      // DoobieAccountRepository.layer.orDie >+>
      UserHttpClient.layer >+>
      AccountLive.layer

  override def run(args: List[String]) = {
    val app =
      konsole.Main.run
      // rest.Main.run
    app.provideCustomLayer(layer)
  }
}
