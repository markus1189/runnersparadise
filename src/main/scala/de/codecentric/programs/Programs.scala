package de.codecentric.programs

import de.codecentric.domain.{RaceId, RunnerId}
import de.codecentric.persistence.{RaceAlg, RegistrationAlg, RunnerAlg}

import scalaz.Scalaz._
import scalaz._

trait Programs {
  def register[F[_]: Monad: RunnerAlg: RaceAlg: RegistrationAlg](
      runnerId: RunnerId,
      raceId: RaceId): F[Either[String, Unit]] = {
    for {
      runner <- OptionT(RunnerAlg().findRunner(runnerId))
      reg    <- OptionT(RegistrationAlg().findReg(raceId))
      newReg <- OptionT(reg.add(runner).pure[F])
      _      <- OptionT(RegistrationAlg().saveReg(newReg).map(Option(_)))
    } yield ()
  }.run.map(_.toRight("Error during registration"))
}

object Programs extends Programs
