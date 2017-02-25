package de.codecentric
package runnersparadise.interpreters.cassandra

import de.codecentric.runnersparadise.algebra.{RaceAlg, RegistrationAlg, RunnerAlg}
import de.codecentric.runnersparadise.domain._

import scalaz.ReaderT
import scalaz.concurrent.Task
import scalaz.syntax.functor._

class Cass[A](val value: ReaderT[Task, RunnersParadiseDb, A]) extends AnyVal {
  def run: RunnersParadiseDb => Task[A] = value.run
}

object Cass {
  def apply[A](f: RunnersParadiseDb => Task[A]): Cass[A] = new Cass(ReaderT(f))

  implicit val algebraInstances: RunnerAlg[Cass] = new RunnerAlg[Cass] with RaceAlg[Cass]
  with RegistrationAlg[Cass] {
    override def saveRunner(runner: Runner): Cass[Unit]          = Cass(_.runners.save(runner).void)
    override def findRunner(id: RunnerId): Cass[Option[Runner]]  = Cass(_.runners.find(id))
    override def listRunners: Cass[Vector[Runner]]               = Cass(_.runners.list)
    override def saveRace(race: Race): Cass[Unit]                = Cass(_.races.save(race).void)
    override def findRace(id: RaceId): Cass[Option[Race]]        = Cass(_.races.find(id))
    override def listRaces: Cass[Vector[Race]]                   = Cass(_.races.list)
    override def findReg(id: RaceId): Cass[Option[Registration]] = Cass(_.findRegistration(id))
    override def saveReg(reg: Registration): Cass[Unit]          = Cass(_.registrations.save(reg))
  }
}
