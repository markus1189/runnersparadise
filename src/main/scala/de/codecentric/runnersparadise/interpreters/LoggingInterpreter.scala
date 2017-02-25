package de.codecentric.runnersparadise.interpreters

import de.codecentric.runnersparadise.algebra.{RaceAlg, RegistrationAlg, RunnerAlg}
import de.codecentric.runnersparadise.domain._
import org.slf4j.{Logger, LoggerFactory}

import scalaz.Const

class LoggingInterpreter(logger: Logger) {
  implicit val runners: RunnerAlg[Const[Unit, ?]] = new RunnerAlg[Const[Unit, ?]] {
    override def saveRunner(runner: Runner): Const[Unit, Unit] =
      Const(logger.debug(s"Saving runner: {}", runner))

    override def findRunner(id: RunnerId): Const[Unit, Option[Runner]] =
      Const(logger.debug(s"Trying to find runner: {}", id))

    override def listRunners: Const[Unit, Vector[Runner]] =
      Const(logger.debug("Listing all runners"))
  }

  implicit val races: RaceAlg[Const[Unit, ?]] = new RaceAlg[Const[Unit, ?]] {
    override def saveRace(race: Race): Const[Unit, Unit] =
      Const(logger.debug(s"Saving race: $race"))

    override def findRace(id: RaceId): Const[Unit, Option[Race]] =
      Const(logger.debug(s"Trying to find race: {}", id))

    override def listRaces: Const[Unit, Vector[Race]] =
      Const(logger.debug("Lising all races"))
  }

  implicit val registrations: RegistrationAlg[Const[Unit, ?]] =
    new RegistrationAlg[Const[Unit, ?]] {
      override def findReg(id: RaceId): Const[Unit, Option[Registration]] =
        Const(logger.debug(s"Trying to find registration: {}", id))

      override def saveReg(reg: Registration): Const[Unit, Unit] =
        Const(logger.debug(s"Saving registration: {}", reg))
    }
}

object LoggingInterpreter
    extends LoggingInterpreter(LoggerFactory.getLogger(classOf[LoggingInterpreter]))
