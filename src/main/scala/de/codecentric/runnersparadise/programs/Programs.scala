package de.codecentric
package runnersparadise.programs

import de.codecentric.runnersparadise.Errors.RegistrationError
import de.codecentric.runnersparadise.Errors.RegistrationError._
import de.codecentric.runnersparadise.algebra.{RaceAlg, RegistrationAlg, RunnerAlg}
import de.codecentric.runnersparadise.domain._

import scalaz.std.vector._
import scalaz.syntax.applicative._
import scalaz.syntax.foldable._
import scalaz.{Monad, OptionT}

trait Programs {
  def register[F[_]: Monad: RunnerAlg: RaceAlg: RegistrationAlg](
      runnerId: RunnerId,
      raceId: RaceId): F[Either[RegistrationError, Registration]] = {
    for {
      runner <- OptionT(RunnerAlg[F].findRunner(runnerId))
        .toRight[RegistrationError](RunnerNotFound(runnerId))
      race <- OptionT(RaceAlg[F].findRace(raceId)).toRight[RegistrationError](RaceNotFound(raceId))
      reg <- OptionT(RegistrationAlg[F].findReg(raceId))
        .orElse(OptionT(Option(Registration(race, Set())).pure[F]))
        .toRight[RegistrationError](RegistrationNotFound(raceId))
      newReg <- OptionT(reg.add(runner).pure[F]).toRight[RegistrationError](RaceHasMaxAttendees)
      _ <- OptionT(RegistrationAlg[F].saveReg(newReg).map(Option(_)))
        .toRight[RegistrationError](RegistrationSaveFailed(None))
    } yield newReg
  }.run.map(_.toEither)

  def registerOpt[F[_]: Monad: RunnerAlg: RaceAlg: RegistrationAlg](
      runnerId: RunnerId,
      raceId: RaceId): F[Option[Registration]] = {

    val M = Monad[OptionT[F, ?]]

    for {
      runner <- OptionT(RunnerAlg[F].findRunner(runnerId))
      race   <- OptionT(RaceAlg[F].findRace(raceId))
      reg    <- OptionT(RegistrationAlg[F].findReg(raceId)).orElse(M.point(Registration(race, Set())))
      newReg <- OptionT(reg.add(runner).pure[F])
      _      <- OptionT(RegistrationAlg[F].saveReg(newReg).map(Option(_)))
    } yield newReg
  }.run

  def demo[F[_]: Monad: RunnerAlg: RaceAlg: RegistrationAlg]: F[Option[Registration]] = {
    val race = Race(RaceId.random(), "The Grand Challenge", 5)
    val runners =
      Vector.tabulate(5)(i => Runner(RunnerId.random(), "runner", s"$i", None))

    val saveRunners = runners.traverse_(runner => RunnerAlg[F].saveRunner(runner))

    val saveRace  = RaceAlg[F].saveRace(race)
    val foundRace = RaceAlg[F].findRace(race.id)
    val registerRunners = runners.traverseU_ { runner =>
      Programs.register[F](runner.id, race.id)
    }

    val findReg = RegistrationAlg[F].findReg(race.id)

    saveRunners *> saveRace *> foundRace *> registerRunners *> findReg
  }

}

object Programs extends Programs
