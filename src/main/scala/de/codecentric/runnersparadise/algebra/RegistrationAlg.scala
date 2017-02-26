package de.codecentric
package runnersparadise.algebra

import de.codecentric.runnersparadise.domain.{RaceId, Registration}

trait RegistrationAlg[F[_]] {
  def saveReg(reg: Registration): F[Unit]

  def findReg(id: RaceId): F[Option[Registration]]
}

object RegistrationAlg {
  def apply[F[_]: RegistrationAlg](): RegistrationAlg[F] = implicitly
}
