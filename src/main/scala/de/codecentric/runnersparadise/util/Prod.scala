package de.codecentric.runnersparadise.util

import de.codecentric.runnersparadise.algebra.{RaceAlg, RegistrationAlg, RunnerAlg}
import de.codecentric.runnersparadise.domain._

import scalaz.Monad

case class Prod[F[_], G[_], A](_1: F[A], _2: G[A])

object Prod {
  implicit def runnerAlg[F[_]: RunnerAlg, G[_]: RunnerAlg]: RunnerAlg[Prod[F, G, ?]] =
    new RunnerAlg[Prod[F, G, ?]] {
      override def findRunner(id: RunnerId): Prod[F, G, Option[Runner]] =
        Prod(RunnerAlg[F]().findRunner(id), RunnerAlg[G]().findRunner(id))

      override def saveRunner(runner: Runner): Prod[F, G, Unit] =
        Prod(RunnerAlg[F]().saveRunner(runner), RunnerAlg[G]().saveRunner(runner))

      override def listRunners: Prod[F, G, Vector[Runner]] =
        Prod(RunnerAlg[F]().listRunners, RunnerAlg[G]().listRunners)
    }

  implicit def raceAlg[F[_]: RaceAlg, G[_]: RaceAlg]: RaceAlg[Prod[F, G, ?]] =
    new RaceAlg[Prod[F, G, ?]] {
      override def saveRace(race: Race): Prod[F, G, Unit] =
        Prod(RaceAlg[F]().saveRace(race), RaceAlg[G]().saveRace(race))

      override def findRace(id: RaceId): Prod[F, G, Option[Race]] =
        Prod(RaceAlg[F]().findRace(id), RaceAlg[G]().findRace(id))

      override def listRaces: Prod[F, G, Vector[Race]] =
        Prod(RaceAlg[F]().listRaces, RaceAlg[G]().listRaces)
    }

  implicit def regAlg[F[_]: RegistrationAlg, G[_]: RegistrationAlg]
    : RegistrationAlg[Prod[F, G, ?]] =
    new RegistrationAlg[Prod[F, G, ?]] {
      override def findReg(id: RaceId): Prod[F, G, Option[Registration]] =
        Prod(RegistrationAlg[F]().findReg(id), RegistrationAlg[G]().findReg(id))

      override def saveReg(reg: Registration): Prod[F, G, Unit] =
        Prod(RegistrationAlg[F]().saveReg(reg), RegistrationAlg[G]().saveReg(reg))
    }

  implicit def prodMonad[F[_]: Monad, G[_]: Monad]: Monad[Prod[F, G, ?]] =
    new Monad[Prod[F, G, ?]] {
      override def point[A](a: => A): Prod[F, G, A] = Prod(Monad[F].point(a), Monad[G].point(a))

      override def bind[A, B](fa: Prod[F, G, A])(f: (A) => Prod[F, G, B]): Prod[F, G, B] = {
        Prod(
          Monad[F].bind(fa._1)(x => f(x)._1),
          Monad[G].bind(fa._2)(x => f(x)._2)
        )
      }
    }
}
