package de.codecentric.persistence

import de.codecentric.domain.{Race, RaceId}

trait RaceAlg[F[_]] {
  def saveRace(race: Race): F[Unit]

  def findRace(id: RaceId): F[Option[Race]]
}

object RaceAlg {
  def apply[F[_]: RaceAlg](): RaceAlg[F] = implicitly
}
