package de.codecentric
package runnersparadise.programs

import de.codecentric.runnersparadise.domain.{RaceId, Registration, RunnerId}
import de.codecentric.runnersparadise.algebra.{RaceAlg, RegistrationAlg, RunnerAlg}

import scalaz.Scalaz._
import scalaz._

trait Programs {
  def register[F[_]: Monad: RunnerAlg: RaceAlg: RegistrationAlg](
      runnerId: RunnerId,
      raceId: RaceId): F[Either[String, Registration]] = {
    for {
      runner <- OptionT(RunnerAlg().findRunner(runnerId)).toRight("Runner not found")
      reg    <- OptionT(RegistrationAlg().findReg(raceId)).toRight("Registration not found")
      newReg <- OptionT(reg.add(runner).pure[F]).toRight("Could not add runner to registration")
      _      <- OptionT(RegistrationAlg().saveReg(newReg).map(Option(_))).toRight("Could not save registrataion")
    } yield newReg
  }.run.map(_.toEither)
}

object Programs extends Programs
