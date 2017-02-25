package de.codecentric
package runnersparadise.interpreters.cassandra.tables

import com.outworkers.phantom.dsl._
import de.codecentric.runnersparadise.domain.{Race, RaceId}

import scala.util.{Failure, Success}
import scalaz.concurrent.Task

abstract class Races extends CassandraTable[Races, Race] with RootConnector {
  object id           extends UUIDColumn(this) with PartitionKey
  object name         extends StringColumn(this)
  object maxAttendees extends LongColumn(this)

  def save(race: Race): Task[ResultSet] = Task.async[ResultSet] { k =>
    insert
      .value(_.id, race.id.value)
      .value(_.name, race.name)
      .value(_.maxAttendees, race.maxAttendees)
      .future()
      .onComplete {
        case Success(rs) => k(rs.right)
        case Failure(e)  => k(e.left)
      }
  }

  def find(raceId: RaceId): Task[Option[Race]] = Task.async[Option[Race]] { k =>
    select.all.where(_.id eqs raceId.value).one().onComplete {
      case Success(rs) => k(rs.right)
      case Failure(e)  => k(e.left)
    }
  }

  def list: Task[Vector[Race]] = {
    Task.async[Vector[Race]] { k =>
      select.all().fetch().map(_.to[Vector]).onComplete {
        case Success(r) => k(r.right)
        case Failure(e) => k(e.left)
      }
    }
  }

  override def fromRow(r: Row): Race = {
    Race(RaceId(id(r)), name(r), maxAttendees(r))
  }
}
