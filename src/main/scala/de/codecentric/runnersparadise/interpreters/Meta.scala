package de.codecentric.runnersparadise.interpreters

import de.codecentric.runnersparadise.algebra.{RaceAlg, RegistrationAlg, RunnerAlg}
import de.codecentric.runnersparadise.domain._

import scalaz.syntax.functor._
import scalaz.syntax.semigroup._
import scalaz.{Functor, Monad, Monoid, StateT}

case class MetaInfo(numFinds: Int, numSaves: Int, numRegs: Int)
object MetaInfo {
  def find: MetaInfo = MetaInfo(1, 0, 0)
  def save: MetaInfo = MetaInfo(0, 1, 0)
  def reg: MetaInfo  = MetaInfo(0, 0, 1)

  implicit val instances: Monoid[MetaInfo] = new Monoid[MetaInfo] {
    override def zero: MetaInfo = MetaInfo(0, 0, 0)

    override def append(f1: MetaInfo, f2: => MetaInfo): MetaInfo =
      MetaInfo(f1.numFinds + f2.numFinds, f1.numSaves + f2.numSaves, f1.numRegs + f2.numRegs)
  }
}

class Meta[F[_], A](val run: StateT[F, MetaInfo, A]) extends AnyVal

object Meta {
  def apply[F[_], A](fa: StateT[F, MetaInfo, A]): Meta[F, A] = {
    new Meta(fa)
  }

  implicit def monadInstance[F[_]](implicit MF: Monad[F]): Monad[Meta[F,?]] = new Monad[Meta[F, ?]] {
    override def point[A](a: => A): Meta[F, A] = Meta(StateT(s => MF.point(a).strengthL(s)))

    override def bind[A, B](fa: Meta[F, A])(f: (A) => Meta[F, B]): Meta[F, B] = {
      Meta {
        fa.run.flatMap(a => f(a).run)
      }
    }
  }

  implicit def algebraInstances[F[_]: Functor: RunnerAlg: RaceAlg: RegistrationAlg]
    : RunnerAlg[Meta[F, ?]] with RaceAlg[Meta[F, ?]] with RegistrationAlg[Meta[F, ?]] =
    new RunnerAlg[Meta[F, ?]] with RaceAlg[Meta[F, ?]] with RegistrationAlg[Meta[F, ?]] {

      override def saveRunner(runner: Runner): Meta[F, Unit] = {
        Meta {
          StateT[F, MetaInfo, Unit](info =>
            RunnerAlg[F].saveRunner(runner).strengthL(info |+| MetaInfo.save))
        }
      }

      override def findRunner(id: RunnerId): Meta[F, Option[Runner]] = {
        Meta {
          StateT[F, MetaInfo, Option[Runner]](info =>
            RunnerAlg[F].findRunner(id).strengthL(info |+| MetaInfo.find))
        }
      }

      override def listRunners: Meta[F, Vector[Runner]] = {
        Meta {
          StateT[F, MetaInfo, Vector[Runner]](info => RunnerAlg[F].listRunners.strengthL(info))
        }
      }

      override def saveRace(race: Race): Meta[F, Unit] = {
        Meta {
          StateT[F, MetaInfo, Unit](info =>
            RaceAlg[F].saveRace(race).strengthL(info |+| MetaInfo.save))
        }
      }

      override def findRace(id: RaceId): Meta[F, Option[Race]] = {
        Meta {
          StateT[F, MetaInfo, Option[Race]](info =>
            RaceAlg[F].findRace(id).strengthL(info |+| MetaInfo.find))
        }
      }

      override def listRaces: Meta[F, Vector[Race]] = {
        Meta {
          StateT[F, MetaInfo, Vector[Race]](info => RaceAlg[F].listRaces.strengthL(info))
        }
      }

      override def saveReg(reg: Registration): Meta[F, Unit] = {
        Meta {
          StateT[F, MetaInfo, Unit](info =>
            RegistrationAlg[F].saveReg(reg).strengthL(info |+| MetaInfo.save))
        }
      }

      override def findReg(id: RaceId): Meta[F, Option[Registration]] = {
        Meta {
          StateT[F, MetaInfo, Option[Registration]](info =>
            RegistrationAlg[F].findReg(id).strengthL(info |+| MetaInfo.find))
        }
      }
    }
}
