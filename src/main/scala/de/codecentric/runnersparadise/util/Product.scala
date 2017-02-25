package de.codecentric.runnersparadise.util

import de.codecentric.runnersparadise.algebra.{RaceAlg, RegistrationAlg, RunnerAlg}
import de.codecentric.runnersparadise.domain._

case class Product[F[_], G[_], A](_1: F[A], _2: G[A])

object Product {
  implicit def runnerAlg[F[_]: RunnerAlg, G[_]: RunnerAlg]: RunnerAlg[Product[F, G, ?]] =
    new RunnerAlg[Product[F, G, ?]] {
      override def findRunner(id: RunnerId): Product[F, G, Option[Runner]] =
        Product(RunnerAlg[F]().findRunner(id), RunnerAlg[G]().findRunner(id))

      override def saveRunner(runner: Runner): Product[F, G, Unit] =
        Product(RunnerAlg[F]().saveRunner(runner), RunnerAlg[G]().saveRunner(runner))

      override def listRunners: Product[F, G, Vector[Runner]] = Product(RunnerAlg[F]().listRunners, RunnerAlg[G]().listRunners)
    }

  implicit def raceAlg[F[_]: RaceAlg, G[_]: RaceAlg]: RaceAlg[Product[F, G, ?]] =
    new RaceAlg[Product[F, G, ?]] {
      override def saveRace(race: Race): Product[F, G, Unit] =
        Product(RaceAlg[F]().saveRace(race), RaceAlg[G]().saveRace(race))

      override def findRace(id: RaceId): Product[F, G, Option[Race]] =
        Product(RaceAlg[F]().findRace(id), RaceAlg[G]().findRace(id))

      override def listRaces: Product[F, G, Vector[Race]] = Product(RaceAlg[F]().listRaces, RaceAlg[G]().listRaces)
    }

  implicit def regAlg[F[_]: RegistrationAlg, G[_]: RegistrationAlg]: RegistrationAlg[Product[F, G, ?]] =
    new RegistrationAlg[Product[F, G, ?]] {
      override def findReg(id: RaceId): Product[F, G, Option[Registration]] =
        Product(RegistrationAlg[F]().findReg(id), RegistrationAlg[G]().findReg(id))

      override def saveReg(reg: Registration): Product[F, G, Unit] =
        Product(RegistrationAlg[F]().saveReg(reg), RegistrationAlg[G]().saveReg(reg))
    }
}
