package de.codecentric
package runnersparadise.algebra

import de.codecentric.runnersparadise.api.RaceRegistrationService.AddRunner
import de.codecentric.runnersparadise.domain.{Runner, RunnerId}

trait RunnerAlg[F[_]] {
  def saveRunner(runner: Runner): F[Unit]

  def findRunner(id: RunnerId): F[Option[Runner]]

  def listRunners: F[Vector[Runner]]
}

object RunnerAlg {
  def apply[F[_]: RunnerAlg]: RunnerAlg[F] = implicitly
}

trait RunnerFunctions {
  def createRunner(cmd: AddRunner): Runner = {
    Runner(RunnerId.random(), cmd.firstname, cmd.lastname, cmd.nickname)
  }
}

object RunnerFunctions extends RunnerFunctions
