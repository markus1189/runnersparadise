package de.codecentric.runnersparadise.interpreters

import de.codecentric.runnersparadise.algebra.{RaceAlg, RegistrationAlg, RunnerAlg}
import de.codecentric.runnersparadise.domain._
import org.slf4j.Logger

import scalaz.{Id, Monad, Reader}

class Log[F[_], A](val value: Reader[Logger, F[A]]) extends AnyVal

object Log {
  def apply[F[_], A](f: Logger => F[A]): Log[F, A] = new Log[F, A](Reader(f))

  implicit def algebraInstances[F[_]: RunnerAlg: RaceAlg: RegistrationAlg]
    : RunnerAlg[Log[F, ?]] with RaceAlg[Log[F, ?]] with RegistrationAlg[Log[F, ?]] = {
    new RunnerAlg[Log[F, ?]] with RaceAlg[Log[F, ?]] with RegistrationAlg[Log[F, ?]] {
      override def saveRunner(runner: Runner): Log[F, Unit] = Log { l =>
        l.debug(s"Saving runner: {}", runner)
        RunnerAlg[F].saveRunner(runner)
      }
      override def findRunner(id: RunnerId): Log[F, Option[Runner]] = Log { l =>
        l.debug(s"Trying to find runner: {}", id)
        RunnerAlg[F].findRunner(id)
      }

      override def listRunners: Log[F, Vector[Runner]] = Log { l =>
        l.debug("Listing all runners")
        RunnerAlg[F].listRunners
      }

      override def saveRace(race: Race): Log[F, Unit] = Log { l =>
        l.debug(s"Saving race: $race")
        RaceAlg[F].saveRace(race)
      }

      override def findRace(id: RaceId): Log[F, Option[Race]] =
        Log { l =>
          l.debug(s"Trying to find race: {}", id)
          RaceAlg[F].findRace(id)
        }

      override def listRaces: Log[F, Vector[Race]] = Log { l =>
        l.debug("Lising all races")
        RaceAlg[F].listRaces
      }

      override def findReg(id: RaceId): Log[F, Option[Registration]] =
        Log { l =>
          l.debug(s"Trying to find registration: {}", id)
          RegistrationAlg[F].findReg(id)
        }

      override def saveReg(reg: Registration): Log[F, Unit] =
        Log { l =>
          l.debug(s"Saving registration: {}", reg)
          RegistrationAlg[F].saveReg(reg)
        }
    }
  }

  implicit def logMonad[F[_]: Monad] = new Monad[Log[F, ?]] {
    override def point[A](a: => A): Log[F, A] = Log(_ => Monad[F].point(a))

    override def bind[A, B](fa: Log[F, A])(f: (A) => Log[F, B]): Log[F, B] = Log { logger =>
      Monad[F].bind(fa.value.run(logger))(x => f(x).value.run(logger))
    }
  }
}
