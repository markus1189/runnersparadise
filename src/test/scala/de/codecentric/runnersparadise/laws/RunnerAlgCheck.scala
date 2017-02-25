package de.codecentric.runnersparadise.laws

import java.util.concurrent.atomic.AtomicReference

import com.outworkers.phantom.connectors.KeySpace
import de.codecentric.UnitSpec
import de.codecentric.runnersparadise.algebra.RunnerAlg
import de.codecentric.runnersparadise.domain.Runner
import de.codecentric.runnersparadise.interpreters.cassandra.{Cass, Keyspaces, RunnersParadiseDb}
import de.codecentric.runnersparadise.interpreters.{Pure, PureState}
import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import org.scalatest.BeforeAndAfterAll
import org.scalatest.prop.PropertyChecks

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
import scalaz.Applicative
import scalaz.concurrent.Task
import scalaz.syntax.applicative._

abstract class RunnerAlgCheck[F[_]: Applicative: RunnerAlg](name: String)
    extends UnitSpec
    with PropertyChecks
    with ArbitraryInstances {

  def run[A](x: F[A]): A

  s"RunnerAlg instance $name" should {
    "satisfy the law that find after save returns the same runner" in {
      forAll { (runner: Runner) =>
        run(RunnerAlg().saveRunner(runner) *> RunnerAlg().findRunner(runner.id)).value should ===(
          runner)
      }
    }

    "be idempotent wrt save" in {
      forAll { (runner: Runner) =>
        run(
          Applicative.apply[F].replicateM_(2, RunnerAlg().saveRunner(runner)) *> RunnerAlg()
            .findRunner(runner.id))
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
  implicit val ec = ExecutionContext.fromExecutor(null)

  EmbeddedCassandraServerHelper.startEmbeddedCassandra()
  val db = new RunnersParadiseDb(Keyspaces.embedded)
  db.create()
  implicit val session = db.session

  Await.result(db.runners.autocreate(KeySpace(Keyspaces.embedded.name)).future(), 30.seconds)

  def run[A](x: Task[A]): A = x.run

  override def afterAll(): Unit = {
    db.shutdown()
    EmbeddedCassandraServerHelper.getCluster.close()
  }

  override def run[A](x: Cass[A]): A = x.run(db).run
}
