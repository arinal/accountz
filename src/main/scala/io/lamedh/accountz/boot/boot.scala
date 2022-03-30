package io.lamedh.accountz.boot

import io.lamedh.accountz.app.konsole
import io.lamedh.accountz.app.rest
import io.lamedh.accountz.infra.repo.inmemory.InMemoryAccountRepository
import io.lamedh.accountz.core.accounts.AccountLive
import zio._

object Boot extends App {

  val layer =
    config.layer >+> InMemoryAccountRepository.layer >+> AccountLive.layer

  override def run(args: List[String]) = {
    val app =
      // konsole.Main.run
      rest.Main.run
    app.provideCustomLayer(layer)
  }
}
