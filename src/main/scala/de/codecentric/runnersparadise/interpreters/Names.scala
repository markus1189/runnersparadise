package de.codecentric.runnersparadise.interpreters

import de.codecentric.runnersparadise.algebra.NameAlg

import scala.util.Random
import scalaz.{Reader, ~>}
import scalaz.concurrent.Task

class RngNames[A](val value: Reader[Random, A]) extends AnyVal

object RngNames {
  def toTask(rng: Random): RngNames ~> Task = new (RngNames ~> Task) {
    override def apply[A](fa: RngNames[A]): Task[A] = Task.delay(fa.value(rng))
  }
  
  def toTaskGlobal: RngNames ~> Task = new (RngNames ~> Task) {
    override def apply[A](fa: RngNames[A]): Task[A] = Task.delay(fa.value(Random))
  }
  
  implicit val algebra: NameAlg[RngNames] = new NameAlg[RngNames] {
    override def randomName: RngNames[(String, String)] = new RngNames(
      Reader[Random, (String, String)] { rng =>
        val first = rng.nextString(10)
        val last  = rng.nextString(15)
        (first, last)
      }
    )
  }
}
