package de.codecentric
package runnersparadise.programs

import de.codecentric.runnersparadise.Errors.RegistrationError
import de.codecentric.runnersparadise.Errors.RegistrationError.{
  RaceHasMaxAttendees,
  RegistrationNotFound,
  RegistrationSaveFailed,
  RunnerNotFound
}
import de.codecentric.runnersparadise.domain.{RaceId, Registration, RunnerId}
import de.codecentric.runnersparadise.algebra.{RaceAlg, RegistrationAlg, RunnerAlg}

import scalaz.Scalaz._
import scalaz._

trait Programs {
  def register[F[_]: Monad: RunnerAlg: RaceAlg: RegistrationAlg](
      runnerId: RunnerId,
      raceId: RaceId): F[Either[RegistrationError, Registration]] = {
    for {
      runner <- OptionT(RunnerAlg().findRunner(runnerId))
        .toRight[RegistrationError](RunnerNotFound(runnerId))
      reg <- OptionT(RegistrationAlg().findReg(raceId))
        .toRight[RegistrationError](RegistrationNotFound(raceId))
      newReg <- OptionT(reg.add(runner).pure[F]).toRight[RegistrationError](RaceHasMaxAttendees)
      _ <- OptionT(RegistrationAlg().saveReg(newReg).map(Option(_)))
        .toRight[RegistrationError](RegistrationSaveFailed(None))
    } yield newReg
  }.run.map(_.toEither)
}

object Programs extends Programs
