package de.codecentric.runnersparadise.interpreters

import de.codecentric.runnersparadise.algebra.{RaceAlg, RegistrationAlg, RunnerAlg}
import de.codecentric.runnersparadise.domain._
import org.slf4j.Logger

import scalaz.{Const, Monad, Reader}

class Log[A](val value: Reader[Logger, Const[Unit, A]]) extends AnyVal

object Log {
  def apply[A](f: Logger => Unit): Log[A] = new Log(Reader(f).map(Const(_)))

  implicit val algebraInstances: RunnerAlg[Log] with RaceAlg[Log] with RegistrationAlg[Log] =
    new RunnerAlg[Log] with RaceAlg[Log] with RegistrationAlg[Log] {
      override def saveRunner(runner: Runner): Log[Unit] =
        Log(_.debug(s"Saving runner: {}", runner))
      override def findRunner(id: RunnerId): Log[Option[Runner]] =
        Log(_.debug(s"Trying to find runner: {}", id))
      override def listRunners: Log[Vector[Runner]] = Log(_.debug("Listing all runners"))
      override def saveRace(race: Race): Log[Unit]  = Log(_.debug(s"Saving race: $race"))
      override def findRace(id: RaceId): Log[Option[Race]] =
        Log(_.debug(s"Trying to find race: {}", id))
      override def listRaces: Log[Vector[Race]] = Log(_.debug("Lising all races"))
      override def findReg(id: RaceId): Log[Option[Registration]] =
        Log(_.debug(s"Trying to find registration: {}", id))
      override def saveReg(reg: Registration): Log[Unit] =
        Log(_.debug(s"Saving registration: {}", reg))
    }
}
