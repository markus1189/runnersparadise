package de.codecentric
package runnersparadise.interpreters

import java.util.concurrent.atomic.AtomicReference
import java.util.function.UnaryOperator

import de.codecentric.runnersparadise.algebra.{RaceAlg, RegistrationAlg, RunnerAlg}
import de.codecentric.runnersparadise.domain._

import scalaz.{Monad, Reader}

case class PureState(runners: Map[RunnerId, Runner],
                     races: Map[RaceId, Race],
                     registrations: Map[RaceId, Registration])
object PureState {
  def empty: PureState = PureState(Map.empty, Map.empty, Map.empty)
}

class Pure[A](val value: Reader[AtomicReference[PureState], A]) extends AnyVal

object Pure {
  implicit class AtomicReferenceOps[A](val value: AtomicReference[A]) extends AnyVal {
    def update(f: A => A): Unit = {
      value.getAndUpdate(new UnaryOperator[A] {
        override def apply(x: A): A = f(x)
      })
      ()
    }
  }
  def apply[A](f: AtomicReference[PureState] => A): Pure[A] = new Pure(Reader(f))

  implicit val algebraInstances: RunnerAlg[Pure] with RaceAlg[Pure] with RegistrationAlg[Pure] =
    new RunnerAlg[Pure] with RaceAlg[Pure] with RegistrationAlg[Pure] {
      override def saveRunner(runner: Runner): Pure[Unit] = Pure { ref =>
        ref.update(s => s.copy(runners = s.runners.updated(runner.id, runner)))
      }

      override def findRunner(id: RunnerId): Pure[Option[Runner]] = Pure { ref =>
        ref.get.runners.get(id)
      }

      override def listRunners: Pure[Vector[Runner]] = Pure { ref =>
        ref.get.runners.values.to[Vector]
      }

      override def saveRace(race: Race): Pure[Unit] = Pure { ref =>
        ref.update(s => s.copy(races = s.races.updated(race.id, race)))
      }

      override def findRace(id: RaceId): Pure[Option[Race]] = Pure { ref =>
        ref.get.races.get(id)
      }

      override def listRaces: Pure[Vector[Race]] = Pure { ref =>
        ref.get.races.values.to[Vector]
      }

      override def findReg(id: RaceId): Pure[Option[Registration]] = Pure { ref =>
        ref.get.registrations.get(id)
      }

      override def saveReg(reg: Registration): Pure[Unit] = Pure { ref =>
        ref.update(s => s.copy(registrations = s.registrations.updated(reg.race.id, reg)))
      }
    }

  implicit val monad: Monad[Pure] = new Monad[Pure] {
    override def point[A](a: => A): Pure[A] = new Pure(implicitly[Monad[Reader[AtomicReference[PureState], ?]]].point(a))
    override def bind[A, B](fa: Pure[A])(f: (A) => Pure[B]): Pure[B] =
      new Pure(implicitly[Monad[Reader[AtomicReference[PureState], ?]]].bind(fa.value)(x => f(x).value))
  }
}
