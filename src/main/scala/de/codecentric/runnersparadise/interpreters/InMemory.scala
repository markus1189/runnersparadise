package de.codecentric
package runnersparadise.interpreters

import de.codecentric.runnersparadise.algebra.{RaceAlg, RegistrationAlg, RunnerAlg}
import de.codecentric.runnersparadise.domain._

import scalaz.Id.Id
import scalaz.concurrent.Task

class InMemory extends {
  var runnerStore: Map[RunnerId, Runner]  = Map()
  var raceStore: Map[RaceId, Race]        = Map()
  var regStore: Map[RaceId, Registration] = Map()

  implicit val runners: RunnerAlg[Id] = new RunnerAlg[Id] {
    override def saveRunner(runner: Runner): Unit = {
      runnerStore = runnerStore.updated(runner.id, runner)
    }

    override def findRunner(id: RunnerId): Option[Runner] = runnerStore.get(id)
  }

  implicit val races: RaceAlg[Id] = new RaceAlg[Id] {
    override def saveRace(race: Race): Unit = {
      raceStore = raceStore.updated(race.id, race)
    }

    override def findRace(id: RaceId): Option[Race] = raceStore.get(id)
  }

  implicit val registrations: RegistrationAlg[Id] = new RegistrationAlg[Id] {
    override def findReg(id: RaceId): Option[Registration] =
      regStore.values.find(_.race.id === id)

    override def saveReg(reg: Registration): Unit = {
      regStore = regStore.updated(reg.race.id, reg)
    }
  }
}
