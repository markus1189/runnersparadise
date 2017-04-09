package de.codecentric
package runnersparadise

import java.util.concurrent.atomic.AtomicReference

import de.codecentric.runnersparadise.api.RaceRegistrationService
import de.codecentric.runnersparadise.interpreters._
import de.codecentric.runnersparadise.interpreters.cassandra.{Cass, RunnersParadiseDb}
import de.codecentric.runnersparadise.util.Prod
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.server.{Server, ServerApp}
import org.slf4j.LoggerFactory

import scalaz.concurrent.Task
import scalaz.~>

object MainInMemory extends ServerApp {
  val state = new AtomicReference(PureState.empty)
  private val natF = new (Pure ~> Task) {
    override def apply[A](fa: Pure[A]): Task[A] = Task.delay(fa.value(state))
  }
  val srv: RaceRegistrationService[Pure, RngNames] =
    new RaceRegistrationService(natF, RngNames.toTaskGlobal)

  override def server(args: List[String]): Task[Server] =
    BlazeBuilder.bindHttp(port = 8080, host = "localhost").mountService(srv.service, "/").start
}

object MainCassandra extends ServerApp {
  val srv: RaceRegistrationService[Cass, RngNames] = new RaceRegistrationService[Cass, RngNames](new (Cass ~> Task) {
    override def apply[A](fa: Cass[A]): Task[A] = fa.run(RunnersParadiseDb)
  }, RngNames.toTaskGlobal)

  override def server(args: List[String]): Task[Server] =
    BlazeBuilder.bindHttp(port = 8080, host = "localhost").mountService(srv.service, "/").start
}

object MainWithCassandraWithLogWithMeta extends ServerApp {
  private val logger = LoggerFactory.getLogger("de.codecentric.MainWithCassandraWithLogWithMeta")

  val srv: RaceRegistrationService[Log[Meta[Cass, ?], ?], RngNames] =
    new RaceRegistrationService[Log[Meta[Cass, ?], ?], RngNames](new (Log[Meta[Cass, ?], ?] ~> Task) {
      override def apply[A](fa: Log[Meta[Cass, ?], A]): Task[A] = {
        fa.value(logger).run.run(MetaInfo(0, 0, 0)).run(RunnersParadiseDb).map {
          case (info, res) =>
            logger.info(s"MetaInfo calculated: $info")
            res
        }
      }
    }, RngNames.toTaskGlobal)

  override def server(args: List[String]): Task[Server] =
    BlazeBuilder.bindHttp(port = 8080, host = "localhost").mountService(srv.service, "/").start
}

object MainProduct extends ServerApp {
  val srv: RaceRegistrationService[Prod[Cass, Pure, ?], RngNames] =
    new RaceRegistrationService[Prod[Cass, Pure, ?], RngNames](new (Prod[Cass, Pure, ?] ~> Task) {
      override def apply[A](fa: Prod[Cass, Pure, A]): Task[A] = fa._1.value.run(RunnersParadiseDb)
    }, RngNames.toTaskGlobal)

  override def server(args: List[String]): Task[Server] =
    BlazeBuilder.bindHttp(port = 8080, host = "localhost").mountService(srv.service, "/").start
}

object MainInMemoryLogging extends ServerApp {
  val state = new AtomicReference(PureState.empty)

  val srv: RaceRegistrationService[Log[Pure, ?], RngNames] =
    new RaceRegistrationService[Log[Pure, ?], RngNames](new (Log[Pure, ?] ~> Task) {
      override def apply[A](fa: Log[Pure, A]): Task[A] = {
        val pure: Pure[A] =
          fa.value.run(LoggerFactory.getLogger("de.codecentric.MainInMemoryLogging"))
        Task.delay(pure.value(state))
      }
    }, RngNames.toTaskGlobal)

  override def server(args: List[String]): Task[Server] =
    BlazeBuilder.bindHttp(port = 8080, host = "localhost").mountService(srv.service, "/").start
}
