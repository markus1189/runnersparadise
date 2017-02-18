package de.codecentric.runnersparadise.persistence

import de.codecentric.runnersparadise.domain.{RaceId, Registration}

trait RegistrationAlg[F[_]] {
  def findReg(id: RaceId): F[Option[Registration]]

  def saveReg(reg: Registration): F[Unit]

  def newReg(raceId: RaceId): F[Option[Registration]]
}

object RegistrationAlg {
  def apply[F[_]: RegistrationAlg](): RegistrationAlg[F] = implicitly
}
