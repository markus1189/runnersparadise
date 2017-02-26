package de.codecentric
package runnersparadise

import java.util.concurrent.atomic.AtomicReference

import de.codecentric.runnersparadise.api.RaceRegistrationService
import de.codecentric.runnersparadise.interpreters.cassandra.{Cass, RunnersParadiseDb}
import de.codecentric.runnersparadise.interpreters.{Log, Pure, PureState}
import de.codecentric.runnersparadise.util.Prod
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.server.{Server, ServerApp}

import scalaz.concurrent.Task
import scalaz.~>

object MainInMemory extends ServerApp {
  val state = new AtomicReference(PureState.empty)
  val srv: RaceRegistrationService[Pure] = new RaceRegistrationService(new (Pure ~> Task) {
    override def apply[A](fa: Pure[A]): Task[A] = Task.delay(fa.value(state))
  })

  override def server(args: List[String]): Task[Server] =
    BlazeBuilder.bindHttp(port = 8080, host = "localhost").mountService(srv.service, "/").start
}

object MainCassandra extends ServerApp {
  val srv: RaceRegistrationService[Cass] = new RaceRegistrationService[Cass](new (Cass ~> Task) {
    override def apply[A](fa: Cass[A]): Task[A] = fa.run(RunnersParadiseDb)
  })

  override def server(args: List[String]): Task[Server] =
    BlazeBuilder.bindHttp(port = 8080, host = "localhost").mountService(srv.service, "/").start
}

object MainProduct extends ServerApp {
  val srv: RaceRegistrationService[Prod[Cass, Pure, ?]] =
    new RaceRegistrationService[Prod[Cass, Pure, ?]](new (Prod[Cass, Pure, ?] ~> Task) {
      override def apply[A](fa: Prod[Cass, Pure, A]): Task[A] = fa._1.value.run(RunnersParadiseDb)
    })

  override def server(args: List[String]): Task[Server] =
    BlazeBuilder.bindHttp(port = 8080, host = "localhost").mountService(srv.service, "/").start
}
