package de.codecentric
package runnersparadise.algebra

import de.codecentric.runnersparadise.domain.{RaceId, Registration}

trait RegistrationAlg[F[_]] {
  def findReg(id: RaceId): F[Option[Registration]]

  def saveReg(reg: Registration): F[Unit]
}

object RegistrationAlg {
  def apply[F[_]: RegistrationAlg](): RegistrationAlg[F] = implicitly
}
