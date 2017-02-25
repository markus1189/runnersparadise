package de.codecentric
package runnersparadise.interpreters.cassandra.tables

import com.outworkers.phantom.dsl._
import de.codecentric.runnersparadise.domain._

import scala.util.{Failure, Success}
import scalaz.concurrent.Task
import scalaz.std.scalaFuture._
import scalaz.std.set._
import scalaz.syntax.foldable._

abstract class Registrations
    extends CassandraTable[Registrations, (UUID, UUID)]
    with RootConnector {
  object race     extends UUIDColumn(this) with PartitionKey
  object attendee extends UUIDColumn(this) with PrimaryKey

  def save(reg: Registration): Task[Unit] = {
    Task.async[Unit] { k =>
      reg.attendees
        .traverseU_((attendee: Runner) =>
          insert.value(_.race, reg.race.id.value).value(_.attendee, attendee.id.value).future())
        .onComplete {
          case Success(()) => k(().right)
          case Failure(e)  => k(e.left)
        }
    }
  }

  def find(raceId: RaceId): Task[List[(RaceId, RunnerId)]] =
    Task.async[List[(RaceId, RunnerId)]] { k =>
      select.all
        .where(_.race eqs raceId.value)
        .fetch()
        .map(_.map { case (race, runner) => RaceId(race) -> RunnerId(runner) })
        .onComplete {
          case Success(r) => k(r.right)
          case Failure(e) => k(e.left)
        }
    }
}
