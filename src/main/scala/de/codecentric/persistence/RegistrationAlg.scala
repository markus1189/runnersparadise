package de.codecentric.persistence

import de.codecentric.domain.{RaceId, Registration}

trait RegistrationAlg[F[_]] {
  def findReg(id: RaceId): F[Option[Registration]]

  def saveReg(reg: Registration): F[Unit]

  def newReg(raceId: RaceId): F[Option[Registration]]
}

object RegistrationAlg {
  def apply[F[_]: RegistrationAlg](): RegistrationAlg[F] = implicitly
}
