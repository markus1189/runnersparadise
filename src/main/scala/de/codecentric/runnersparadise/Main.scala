package de.codecentric
package runnersparadise

import de.codecentric.runnersparadise.api.RaceRegistrationService
import de.codecentric.runnersparadise.interpreters.InMemory
import de.codecentric.runnersparadise.interpreters.cassandra.CassandraInterpreter
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.server.{Server, ServerApp}

import scalaz.concurrent.Task

object MainInMemory extends ServerApp {
  val interpreter = new InMemory
  import interpreter._

  val srv: RaceRegistrationService = new RaceRegistrationService

  override def server(args: List[String]): Task[Server] =
    BlazeBuilder.bindHttp(port = 8080, host = "localhost").mountService(srv.service, "/").start
}

object MainCassandra extends ServerApp {
  val interpreter = new CassandraInterpreter
  import interpreter._

  val srv: RaceRegistrationService = new RaceRegistrationService

  override def server(args: List[String]): Task[Server] =
    BlazeBuilder.bindHttp(port = 8080, host = "localhost").mountService(srv.service, "/").start
}