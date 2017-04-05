package de.codecentric.runnersparadise.laws

import java.util.concurrent.atomic.AtomicReference

import com.outworkers.phantom.connectors.KeySpace
import de.codecentric.UnitSpec
import de.codecentric.runnersparadise.algebra.RunnerAlg
import de.codecentric.runnersparadise.domain.Runner
import de.codecentric.runnersparadise.interpreters.cassandra.{Cass, Keyspaces, RunnersParadiseDb}
import de.codecentric.runnersparadise.interpreters.{Pure, PureState}
import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import org.scalacheck.Shrink
import org.scalatest.BeforeAndAfterAll
import org.scalatest.prop.PropertyChecks

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
import scalaz.Applicative
import scalaz.std.vector._
import scalaz.syntax.applicative._
import scalaz.syntax.traverse._

abstract class RunnerAlgCheck[F[_]: Applicative: RunnerAlg](name: String)
    extends UnitSpec
    with PropertyChecks
    with ArbitraryInstances {
  def run[A](x: F[A]): A

  s"RunnerAlg instance $name" should {
    "satisfy the law that find after save returns the same runner" in {
      forAll { (runner: Runner) =>
        run(RunnerAlg[F].saveRunner(runner) *> RunnerAlg[F].findRunner(runner.id)).value should ===(
          runner)
      }
    }

    "be idempotent wrt save" in {
      forAll { (runner: Runner) =>
        def saveN(n: Int): F[Option[Runner]] =
          Applicative.apply[F].replicateM_(n, RunnerAlg[F].saveRunner(runner)) *> RunnerAlg[F]
            .findRunner(runner.id)

        val saveOnce  = saveN(1)
        val saveTwice = saveN(2)

        val (once, twice) = run(saveOnce.tuple(saveTwice))
        once.value should ===(twice.value)
      }
    }

    "relate save and list" in {
      implicit def shrink[A]: Shrink[A] = Shrink(_ => Stream())

      forAll { (initial: Vector[Runner], newRunner: Runner) =>
        val saveInitial = initial.traverse_(RunnerAlg[F].saveRunner)
        val listed = run {
          saveInitial *> RunnerAlg[F].saveRunner(newRunner) *> RunnerAlg[F].listRunners
        }

        listed.sorted should ===((initial :+ newRunner).sorted)
      }
    }
  }
}

class RunnerAlgInMemoryCheck extends RunnerAlgCheck[Pure]("InMemory") {
  override def run[A](x: Pure[A]): A = {
    val s = new AtomicReference(PureState.empty)
    x.value(s)
  }
}

class RunnerAlgCassandraCheck extends RunnerAlgCheck[Cass]("Cassandra") with BeforeAndAfterAll {

  implicit val ec       = ExecutionContext.fromExecutor(null)
  val keyspaceDef       = Keyspaces.embedded
  implicit val keyspace = KeySpace(keyspaceDef.name)

  EmbeddedCassandraServerHelper.startEmbeddedCassandra()

  val db = new RunnersParadiseDb(keyspaceDef)
  db.create()
  implicit val session = db.session

  Await.result(db.runners.autocreate(keyspace).future(), 30.seconds)

  override def afterAll(): Unit = {
    db.shutdown()
    EmbeddedCassandraServerHelper.getCluster.close()
  }

  override def run[A](x: Cass[A]): A = {
    Await.result(db.runners.truncate().future(), 42.seconds)
    x.run(db).run
  }
}
