package de.codecentric
package runnersparadise.algebra

import de.codecentric.runnersparadise.api.RaceRegistrationService.AddRunner
import de.codecentric.runnersparadise.domain.{Runner, RunnerId}

trait RunnerAlg[F[_]] {
  def saveRunner(runner: Runner): F[Unit]

  def findRunner(id: RunnerId): F[Option[Runner]]
}

object RunnerAlg {
  def apply[F[_]: RunnerAlg](): RunnerAlg[F] = implicitly

  implicit def tuple[F[_],G[_]](implicit F: RunnerAlg[F], G: RunnerAlg[G]) = new RunnerAlg[Lambda[A => (F[A],G[A])]] {

    override def saveRunner(runner: Runner): (F[Unit], G[Unit]) = (F.saveRunner(runner), G.saveRunner(runner))

    override def findRunner(id: RunnerId): (F[Option[Runner]], G[Option[Runner]]) = (F.findRunner(id), G.findRunner(id))

  }
}

trait RunnerFunctions {
  def createRunner(cmd: AddRunner): Runner = {
    Runner(RunnerId.random(), cmd.firstname, cmd.lastname, cmd.nickname)
  }
}

object RunnerFunctions extends RunnerFunctions
