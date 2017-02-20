package de.codecentric
package runnersparadise.interpreters.cassandra

import com.outworkers.phantom.connectors.CassandraConnection
import com.outworkers.phantom.dsl._
import de.codecentric.runnersparadise.domain.{RaceId, Registration}
import de.codecentric.runnersparadise.interpreters.cassandra.tables.{Races, Registrations, Runners}

import scalaz.concurrent.Task
import scalaz.std.list._
import scalaz.std.option._
import scalaz.std.scalaFuture._
import scalaz.syntax.traverse._

class LocalDatabase(override val connector: CassandraConnection)
    extends Database[LocalDatabase](connector) {

  lazy val runners: Runners             = new Runners with connector.Connector
  lazy val races: Races                 = new Races with connector.Connector
  lazy val registrations: Registrations = new Registrations with connector.Connector
}

object LocalDatabase extends LocalDatabase(Keyspaces.local) {
  def findRegistration(raceId: RaceId): Task[Option[Registration]] = {
    for {
      ids     <- registrations.find(raceId)
      race    <- ids.headOption.traverseU { case (id, _) => races.find(id) }.map(_.flatten)
      runners <- ids.map(_._2).traverse(runners.find).map(_.flatten)
    } yield {
      race.map(Registration(_, runners.to[Set]))
    }

  }
  def main(args: Array[String]): Unit = {
    LocalDatabase.create()

    List(
      LocalDatabase.runners.create.ifNotExists().future(),
      LocalDatabase.races.create.ifNotExists().future(),
      LocalDatabase.registrations.create.ifNotExists().future()
    ).sequence_.onComplete(_ => shutdown())
  }
}
