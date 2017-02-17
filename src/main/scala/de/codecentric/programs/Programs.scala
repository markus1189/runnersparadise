package de.codecentric.programs

import de.codecentric.domain.{Race, RaceId, RunnerId}
import de.codecentric.persistence.{RaceAlg, RunnerAlg}

import scalaz.Scalaz._
import scalaz._

trait Programs {
  def register[F[_] : Monad : RunnerAlg : RaceAlg](runnerId: RunnerId, raceId: RaceId): F[Option[Race]] = {
    for {
      runner <- OptionT(RunnerAlg().findRunner(runnerId))
      race <- OptionT(RaceAlg().findRace(raceId))
      newRace <- OptionT(race.register(runner).pure[F])
    } yield newRace
  }.run
}

object Programs extends Programs