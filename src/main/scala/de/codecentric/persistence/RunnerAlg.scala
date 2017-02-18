package de.codecentric
package persistence

import de.codecentric.domain.{Runner, RunnerId}
import de.codecentric.services.RaceService.AddRunner

trait RunnerAlg[F[_]] {
  def saveRunner(runner: Runner): F[Unit]

  def findRunner(id: RunnerId): F[Option[Runner]]
}

object RunnerAlg {
  def apply[F[_]: RunnerAlg](): RunnerAlg[F] = implicitly
}

trait RunnerFunctions {
  def createRunner(cmd: AddRunner): Runner = {
    Runner(RunnerId.random(), cmd.firstname, cmd.lastname, cmd.nickname)
  }
}

object RunnerFunctions extends RunnerFunctions
