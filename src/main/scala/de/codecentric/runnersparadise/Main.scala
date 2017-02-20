package de.codecentric
package runnersparadise

import de.codecentric.runnersparadise.interpreters.InMemory
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.server.{Server, ServerApp}

import scalaz.concurrent.Task

object Main extends ServerApp {
  val interpreter = new InMemory
  import interpreter._

  val srv: RaceRegistrationService = new RaceRegistrationService

  override def server(args: List[String]): Task[Server] =
    BlazeBuilder.bindHttp(port = 8080, host = "localhost").mountService(srv.service, "/").start
}
