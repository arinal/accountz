import io.lamedh.accountz.core.accounts.Balance
import io.lamedh.accountz.core.accounts.Account
import io.lamedh.common
import zio._

// Applicative validation //////////////////////////////////////////////////////////////////////////////////////////////
//
val past = common.today()
past.setDate(1)

val acc = Account.checkingAccount(
  no = "1234",
  name = "Damn",
  openDate = Some(common.today()),
  closeDate = Some(past),
  balance = Balance()
)





// Has /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
val has1 = Has(1)
val has2 = has1 ++ Has(2)
val has3 = has2 ++ Has("hello")

has3.get[Int]
has3.get[String]





// Has with service ////////////////////////////////////////////////////////////////////////////////////////////////////
//
trait ServiceA { def get(id: Int): ZIO[Any, Nothing, String] }
trait ServiceB { def get(id: Int): ZIO[Any, Nothing, String] }

object ServiceA {
  def get(id: Int): ZIO[ServiceA, Nothing, String] = ZIO.accessM[ServiceA](_.get(id))
  // def get(id: Int): ZIO[Has[ServiceA], Nothing, String] = ZIO.accessM[Has[ServiceA]](_.get.get(id))
}
object ServiceB {
  def get(id: Int): ZIO[ServiceB, Nothing, String] = ZIO.accessM[ServiceB](_.get(id))
  // def get(id: Int): ZIO[Has[ServiceB], Nothing, String] = ZIO.accessM[Has[ServiceB]](_.get.get(id))
}

class ServiceALive extends ServiceA {
  def get(id: Int): ZIO[Any, Nothing, String] = ZIO.succeed("A")
}
class ServiceBLive extends ServiceB {
  def get(id: Int): ZIO[Any, Nothing, String] = ZIO.succeed("B")
}

Runtime.default.unsafeRun(ServiceA.get(1).provide(new ServiceALive))

val zioOp = for {
  a <- ServiceA.get(1)
  b <- ServiceB.get(1)
} yield (a, b)

// Runtime.default.unsafeRun(zioOp)

// object AB extends ServiceALive with ServiceBLive
// Runtime.default.unsafeRun(zioOp.provide(AB))

// val hasses: Has[ServiceA] with Has[ServiceB] = Has(new ServiceALive: ServiceA) ++ Has(new ServiceBLive: ServiceB)
// Runtime.default.unsafeRun(zioOp.provide(hasses))





// ZLayer //////////////////////////////////////////////////////////////////////////////////////////////////////////////
// ZLayer is a wrapper of Has, it makes Has more composable
// it's a function to create Has

trait ServiceC { def get(id: Int): ZIO[Any, Nothing, String] }
object ServiceC {
  def get(id: Int): ZIO[Has[ServiceC], Nothing, String] = ZIO.accessM[Has[ServiceC]](_.get.get(id))
}
class ServiceCLive(svcA: ServiceA, svcB: ServiceB) extends ServiceC {
  def get(id: Int): ZIO[Any, Nothing, String] = for {
    a <- svcA.get(id)
    b <- svcB.get(id)
  } yield a + b
}

val hasAandB: Has[ServiceA] with Has[ServiceB] = Has(new ServiceALive: ServiceA) ++ Has(new ServiceBLive: ServiceB)
val svcC = new ServiceCLive(hasAandB.get[ServiceA], hasAandB.get[ServiceB])
val hasABC = hasAandB ++ Has(svcC: ServiceC)

Runtime.default.unsafeRun(ServiceC.get(1).provide(hasABC))

val aLayer = ZLayer.fromEffect(ZIO(new ServiceALive: ServiceA))
val bLayer = ZLayer.fromEffect(ZIO(new ServiceBLive: ServiceB))
val abLayer = aLayer ++ bLayer
val abcLayer = abLayer >>> ZLayer.fromServices[ServiceA, ServiceB, ServiceC](new ServiceCLive(_, _))

Runtime.default.unsafeRun {
  abcLayer.build.use { has => has.get[ServiceC].get(1) }
}

Runtime.default.unsafeRun(ServiceC.get(1).provideLayer(abcLayer))
