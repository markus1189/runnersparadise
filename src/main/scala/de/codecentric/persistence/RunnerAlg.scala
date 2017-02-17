package de.codecentric
package persistence

import de.codecentric.domain.{Runner, RunnerId}

trait RunnerAlg[F[_]] {
  def save(runner: Runner): F[Unit]

  def find(id: RunnerId): F[Option[Runner]]
}

object RunnerAlg {
  def apply[F[_]:RunnerAlg](): RunnerAlg[F] = implicitly
}