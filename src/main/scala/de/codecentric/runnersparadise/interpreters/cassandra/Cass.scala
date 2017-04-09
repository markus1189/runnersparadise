package de.codecentric
package runnersparadise.interpreters.cassandra

import de.codecentric.runnersparadise.algebra.{RaceAlg, RegistrationAlg, RunnerAlg}
import de.codecentric.runnersparadise.domain._

import scalaz.concurrent.Task
import scalaz.syntax.functor._
import scalaz.{Monad, ReaderT, ~>}

class Cass[A](val value: ReaderT[Task, RunnersParadiseDb, A]) extends AnyVal {
  def run: RunnersParadiseDb => Task[A] = value.run
}

object Cass {
  def apply[A](f: RunnersParadiseDb => Task[A]): Cass[A] = new Cass(ReaderT(f))

  def toTask(db: RunnersParadiseDb): Cass ~> Task = new (Cass ~> Task) {
    override def apply[A](fa: Cass[A]): Task[A] = fa.run(db)
  }

  implicit val algebraInstances: RunnerAlg[Cass] with RaceAlg[Cass] with RegistrationAlg[Cass] =
    new RunnerAlg[Cass] with RaceAlg[Cass] with RegistrationAlg[Cass] {
      override def saveRunner(runner: Runner): Cass[Unit]          = Cass(_.runners.save(runner).void)
      override def findRunner(id: RunnerId): Cass[Option[Runner]]  = Cass(_.runners.find(id))
      override def listRunners: Cass[Vector[Runner]]               = Cass(_.runners.list)
      override def saveRace(race: Race): Cass[Unit]                = Cass(_.races.save(race).void)
      override def findRace(id: RaceId): Cass[Option[Race]]        = Cass(_.races.find(id))
      override def listRaces: Cass[Vector[Race]]                   = Cass(_.races.list)
      override def findReg(id: RaceId): Cass[Option[Registration]] = Cass(_.findRegistration(id))
      override def saveReg(reg: Registration): Cass[Unit]          = Cass(_.registrations.save(reg))
    }

  implicit val monadInstance: Monad[Cass] = new Monad[Cass] {
    override def point[A](a: => A): Cass[A] = Cass(_ => Task.delay(a))
    override def bind[A, B](fa: Cass[A])(f: (A) => Cass[B]): Cass[B] =
      new Cass(fa.value.flatMap(x => f(x).value))
  }
}
