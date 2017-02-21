package de.codecentric.runnersparadise.interpreters

import de.codecentric.runnersparadise.algebra.{RaceAlg, RegistrationAlg, RunnerAlg}
import de.codecentric.runnersparadise.domain._

import scalaz.Const

class Logger {
  implicit val runners: RunnerAlg[Const[Unit, ?]] = new RunnerAlg[Const[Unit, ?]] {
    override def saveRunner(runner: Runner): Const[Unit, Unit] =
      Const(println(s"Saving runner: $runner"))

    override def findRunner(id: RunnerId): Const[Unit, Option[Runner]] =
      Const(println(s"Trying to find runner: $id"))
  }

  implicit val races: RaceAlg[Const[Unit, ?]] = new RaceAlg[Const[Unit, ?]] {
    override def saveRace(race: Race): Const[Unit, Unit] = Const(println(s"Saving race: $race"))

    override def findRace(id: RaceId): Const[Unit, Option[Race]] =
      Const(println(s"Trying to find race: $id"))
  }

  implicit val registrations: RegistrationAlg[Const[Unit, ?]] =
    new RegistrationAlg[Const[Unit, ?]] {
      override def findReg(id: RaceId): Const[Unit, Option[Registration]] =
        Const(println(s"Trying to find registration: $id"))

      override def saveReg(reg: Registration): Const[Unit, Unit] =
        Const(println(s"Saving registration: $reg"))
    }
}
