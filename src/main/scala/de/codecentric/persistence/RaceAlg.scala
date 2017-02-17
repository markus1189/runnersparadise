package de.codecentric.persistence

import de.codecentric.domain.{Race, RaceId}

trait RaceAlg[F[_]] {
  def save(race: Race): F[Unit]

  def find(id: RaceId): F[Option[Race]]
}

object RaceAlg {
  def apply[F[_]: RaceAlg](): RaceAlg[F] = implicitly
}