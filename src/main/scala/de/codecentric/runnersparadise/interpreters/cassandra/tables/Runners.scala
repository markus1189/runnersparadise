package de.codecentric
package runnersparadise.interpreters.cassandra.tables

import com.outworkers.phantom.dsl._
import de.codecentric.runnersparadise.domain.{Runner, RunnerId}

import scala.util.{Failure, Success}
import scalaz.concurrent.Task

abstract class Runners extends CassandraTable[Runners, Runner] with RootConnector {
  object id        extends UUIDColumn(this) with PartitionKey
  object firstname extends StringColumn(this)
  object lastname  extends StringColumn(this)
  object nickname  extends OptionalStringColumn(this)

  def save(runner: Runner): Task[ResultSet] = {
    Task.async[ResultSet] { k =>
      insert
        .value(_.id, runner.id.value)
        .value(_.firstname, runner.firstname)
        .value(_.lastname, runner.lastname)
        .value(_.nickname, runner.nickname)
        .future()
        .onComplete {
          case Success(rs) => k(rs.right)
          case Failure(e)  => k(e.left)
        }
    }
  }

  def find(id: RunnerId): Task[Option[Runner]] = {
    Task.async[Option[Runner]] { k =>
      select.all().where(_.id eqs id.value).one().onComplete {
        case Success(r) => k(r.right)
        case Failure(e) => k(e.left)
      }
    }
  }
}
