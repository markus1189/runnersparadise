package de.codecentric
package runnersparadise.algebra

import java.util.UUID

import de.codecentric.runnersparadise.domain.{Runner, RunnerId}

import scalaz.Free.FreeC
import scalaz.std.option._
import scalaz.syntax.foldable._
import scalaz.{Free, Monad, ~>}

sealed trait RunnerAlgFree[A]
case class SaveRunner(runner: Runner) extends RunnerAlgFree[Unit]
case class FindRunner(id: RunnerId) extends RunnerAlgFree[Option[Runner]]
case object ListRunners extends RunnerAlgFree[Vector[Runner]]

object RunnerAlgFree {
  def saveRunner(runner: Runner): FreeC[RunnerAlgFree, Unit] = Free.liftFC(SaveRunner(runner))
  def findRunner(id: RunnerId): FreeC[RunnerAlgFree, Option[Runner]] = Free.liftFC(FindRunner(id))
  def listRunners: FreeC[RunnerAlgFree, Vector[Runner]] = Free.liftFC(ListRunners)

  val program: FreeC[RunnerAlgFree, Vector[Runner]] = for {
    r <- findRunner(RunnerId(UUID.randomUUID()))
    _ <- r.traverseU_(saveRunner)
    rs <- listRunners
  } yield rs

  def finalProgram[F[_]:RunnerAlg:Monad]: F[Vector[Runner]] = finalize[Vector[Runner], F](program)

  def finalize[R, F[_]:Monad:RunnerAlg](p: FreeC[RunnerAlgFree, R]): F[R] = Free.runFC(p)(new (RunnerAlgFree ~> F) {
    override def apply[A](fa: RunnerAlgFree[A]): F[A] = fa match {
      case SaveRunner(runner) => RunnerAlg[F].saveRunner(runner)
      case FindRunner(id) => RunnerAlg[F].findRunner(id)
      case ListRunners => RunnerAlg[F].listRunners
    }
  })

  implicit val initialize = new RunnerAlg[FreeC[RunnerAlgFree, ?]] {
    val RAF = RunnerAlgFree
    
    override def saveRunner(runner: Runner): FreeC[RunnerAlgFree, Unit] = RAF.saveRunner(runner)

    override def findRunner(id: RunnerId): FreeC[RunnerAlgFree, Option[Runner]] = RAF.findRunner(id)

    override def listRunners: FreeC[RunnerAlgFree, Vector[Runner]] = RAF.listRunners
  }
}
