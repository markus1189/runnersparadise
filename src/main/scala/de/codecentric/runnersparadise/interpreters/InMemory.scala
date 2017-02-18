package de.codecentric.runnersparadise.interpreters

import de.codecentric.runnersparadise.domain._
import de.codecentric.runnersparadise.persistence.{RaceAlg, RegistrationAlg, RunnerAlg}

import scalaz.concurrent.Task
import scalaz.syntax.equal._

object InMemory extends {
  private var runnerStore: Map[RunnerId, Runner]  = Map()
  private var raceStore: Map[RaceId, Race]        = Map()
  private var regStore: Map[RaceId, Registration] = Map()

  implicit val runners: RunnerAlg[Task] = new RunnerAlg[Task] {
    override def saveRunner(runner: Runner): Task[Unit] = {
      runnerStore = runnerStore.updated(runner.id, runner)
      Task(())
    }

    override def findRunner(id: RunnerId): Task[Option[Runner]] = Task(runnerStore.get(id))
  }

  implicit val races: RaceAlg[Task] = new RaceAlg[Task] {
    override def saveRace(race: Race): Task[Unit] = {
      raceStore = raceStore.updated(race.id, race)
      Task(())
    }

    override def findRace(id: RaceId): Task[Option[Race]] = Task(raceStore.get(id))
  }

  implicit val registrations: RegistrationAlg[Task] = new RegistrationAlg[Task] {
    override def findReg(id: RaceId): Task[Option[Registration]] =
      Task(regStore.values.find(_.race.id === id))

    override def saveReg(reg: Registration): Task[Unit] = {
      regStore = regStore.updated(reg.race.id, reg)
      Task(())
    }

    override def newReg(raceId: RaceId): Task[Option[Registration]] = {
      Task(raceStore.get(raceId).map { race =>
        val reg = Registration(race, Vector())
        regStore = regStore.updated(reg.race.id, reg)
        reg
      })
    }
  }
}
