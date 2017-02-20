package de.codecentric
package runnersparadise.interpreters.cassandra

import de.codecentric.runnersparadise.algebra.{RaceAlg, RegistrationAlg, RunnerAlg}
import de.codecentric.runnersparadise.domain._

import scalaz.OptionT
import scalaz.concurrent.Task
import scalaz.syntax.functor._

class CassandraInterpreter {

  implicit val runners: RunnerAlg[Task] = new RunnerAlg[Task] {
    override def saveRunner(runner: Runner): Task[Unit] = LocalDatabase.runners.save(runner).void

    override def findRunner(id: RunnerId): Task[Option[Runner]] = LocalDatabase.runners.find(id)
  }

  implicit val races: RaceAlg[Task] = new RaceAlg[Task] {
    override def saveRace(race: Race): Task[Unit] = LocalDatabase.races.save(race).void

    override def findRace(id: RaceId): Task[Option[Race]] = LocalDatabase.races.find(id)
  }

  implicit val registrations = new RegistrationAlg[Task] {
    override def findReg(id: RaceId): Task[Option[Registration]] = LocalDatabase.findRegistration(id)

    override def saveReg(reg: Registration): Task[Unit] = LocalDatabase.registrations.save(reg)

    override def newReg(raceId: RaceId): Task[Option[Registration]] = {
      for {
        race <- OptionT(LocalDatabase.races.find(raceId))
        reg = Registration(race, Set())
        _ <- OptionT(saveReg(reg).map(Option(_)))
      } yield reg
    }.run
  }
}
