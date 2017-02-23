package de.codecentric
package runnersparadise.interpreters.cassandra

import de.codecentric.runnersparadise.algebra.{RaceAlg, RegistrationAlg, RunnerAlg}
import de.codecentric.runnersparadise.domain._

import scalaz.concurrent.Task
import scalaz.syntax.functor._

class CassandraInterpreter(db: RunnersParadiseDb) {

  implicit val runners: RunnerAlg[Task] = new RunnerAlg[Task] {
    override def saveRunner(runner: Runner): Task[Unit] = db.runners.save(runner).void

    override def findRunner(id: RunnerId): Task[Option[Runner]] = db.runners.find(id)
  }

  implicit val races: RaceAlg[Task] = new RaceAlg[Task] {
    override def saveRace(race: Race): Task[Unit] = db.races.save(race).void

    override def findRace(id: RaceId): Task[Option[Race]] = db.races.find(id)
  }

  implicit val registrations = new RegistrationAlg[Task] {
    override def findReg(id: RaceId): Task[Option[Registration]] = {
      db.findRegistration(id).map { x =>
        println(x)
        x
      }
    }

    override def saveReg(reg: Registration): Task[Unit] = {
      db.registrations.save(reg)
    }
  }
}
