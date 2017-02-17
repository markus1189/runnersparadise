package de.codecentric
package persistence

import de.codecentric.domain.{Runner, RunnerId}

trait RunnerAlg[F[_]] {
  def saveRunner(runner: Runner): F[Unit]

  def findRunner(id: RunnerId): F[Option[Runner]]
}

object RunnerAlg {
  def apply[F[_]:RunnerAlg](): RunnerAlg[F] = implicitly
}