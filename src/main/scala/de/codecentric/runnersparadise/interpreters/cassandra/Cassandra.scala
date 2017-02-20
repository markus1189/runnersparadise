package de.codecentric.runnersparadise.interpreters.cassandra

import de.codecentric.runnersparadise.algebra.RunnerAlg
import de.codecentric.runnersparadise.domain.{Runner, RunnerId}

import scalaz.concurrent.Task

class Cassandra {
  implicit val runner = new RunnerAlg[Task] {
    override def saveRunner(runner: Runner): Task[Unit] = ???

    override def findRunner(id: RunnerId): Task[Option[Runner]] = ???
  }
}
