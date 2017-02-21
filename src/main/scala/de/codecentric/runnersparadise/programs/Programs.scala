package de.codecentric
package runnersparadise.programs

import de.codecentric.runnersparadise.Errors.RegistrationError
import de.codecentric.runnersparadise.Errors.RegistrationError._
import de.codecentric.runnersparadise.algebra.{RaceAlg, RegistrationAlg, RunnerAlg}
import de.codecentric.runnersparadise.domain._

import scalaz.syntax.applicative._
import scalaz.{Foldable, Monad, OptionT}

trait Programs {
  def register[F[_]: Monad: RunnerAlg: RaceAlg: RegistrationAlg](
      runnerId: RunnerId,
      raceId: RaceId): F[Either[RegistrationError, Registration]] = {
    for {
      runner <- OptionT(RunnerAlg().findRunner(runnerId))
        .toRight[RegistrationError](RunnerNotFound(runnerId))
      race <- OptionT(RaceAlg().findRace(raceId)).toRight[RegistrationError](RaceNotFound(raceId))
      reg <- OptionT(RegistrationAlg().findReg(raceId))
        .orElse(OptionT(Option(Registration(race, Set())).pure[F]))
        .toRight[RegistrationError](RegistrationNotFound(raceId))
      newReg <- OptionT(reg.add(runner).pure[F]).toRight[RegistrationError](RaceHasMaxAttendees)
      _ <- OptionT(RegistrationAlg().saveReg(newReg).map(Option(_)))
        .toRight[RegistrationError](RegistrationSaveFailed(None))
    } yield newReg
  }.run.map(_.toEither)

  def demo[F[_]: Monad: RunnerAlg: RaceAlg: RegistrationAlg]: F[Option[Registration]] = {
    val race = Race(RaceId.random(), "The Grand Challenge", 5)
    println(race.id)
    val runners =
      List.tabulate(5)(i => Runner(RunnerId.random(), "runner", s"$i", None))

    val saveRunners = Foldable[List](scalaz.std.list.listInstance).traverse_(runners)(runner =>
      RunnerAlg().saveRunner(runner))

    val saveRace  = RaceAlg().saveRace(race)
    val foundRace = RaceAlg().findRace(race.id)
    val registerRunners = Foldable[List](scalaz.std.list.listInstance).traverse_(runners) {
      runner =>
        println(s"Trying to register ${runner.id} for ${race.id}")
        Programs.register(runner.id, race.id)
    }

    val findReg = RegistrationAlg().findReg(race.id)

    saveRunners *> saveRace *> foundRace *> registerRunners *> findReg
  }

}

object Programs extends Programs
