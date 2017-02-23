package de.codecentric.runnersparadise.laws

import com.outworkers.phantom.connectors.KeySpace
import de.codecentric.UnitSpec
import de.codecentric.runnersparadise.algebra.RunnerAlg
import de.codecentric.runnersparadise.domain.Runner
import de.codecentric.runnersparadise.interpreters.InMemory
import de.codecentric.runnersparadise.interpreters.cassandra.{
  CassandraInterpreter,
  Keyspaces,
  RunnersParadiseDb
}
import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import org.scalatest.BeforeAndAfterAll
import org.scalatest.prop.PropertyChecks

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._
import scalaz.concurrent.Task
import scalaz.syntax.applicative._
import scalaz.{Applicative, Id}

abstract class RunnerAlgCheck[F[_]: Applicative](name: String)
    extends UnitSpec
    with PropertyChecks
    with ArbitraryInstances {

  implicit def instance: RunnerAlg[F]

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

class RunnerAlgInMemoryCheck extends RunnerAlgCheck[Id.Id]("InMemory") {
  def run[A](x: Id.Id[A]): A = x

  override implicit val instance: RunnerAlg[Id.Id] = {
    val inMemory = new InMemory
    inMemory.runners
  }
}

class RunnerAlgCassandraCheck extends RunnerAlgCheck[Task]("Cassandra") with BeforeAndAfterAll {
  implicit val ec = ExecutionContext.fromExecutor(null)

  EmbeddedCassandraServerHelper.startEmbeddedCassandra()
  val db = new RunnersParadiseDb(Keyspaces.embedded)
  db.create()
  implicit val session = db.session

  Await.result(db.runners.autocreate(KeySpace(Keyspaces.embedded.name)).future(), 30.seconds)

  def run[A](x: Task[A]): A = x.run

  override implicit val instance: RunnerAlg[Task] = {
    val cassandra = new CassandraInterpreter(new RunnersParadiseDb(Keyspaces.embedded))

    cassandra.runners
  }

  override def afterAll(): Unit = {
    db.shutdown()
    EmbeddedCassandraServerHelper.getCluster.close()
  }
}
