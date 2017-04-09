package de.codecentric.runnersparadise.algebra

trait NameAlg[F[_]] {
  def randomName: F[(String,String)]
}

object NameAlg {
  def apply[F[_]:NameAlg]: NameAlg[F] = implicitly
}

