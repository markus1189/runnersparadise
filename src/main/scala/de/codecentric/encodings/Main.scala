package de.codecentric
package encodings

import de.codecentric.domain.{Race, RaceId, Runner, RunnerId}
import de.codecentric.persistence.{RaceAlg, RunnerAlg}
import de.codecentric.services.RaceService
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.server.{Server, ServerApp}

import scalaz.concurrent.Task

object Main extends ServerApp {
  var runnerStore: Map[RunnerId, Runner] = Map()
  var raceStore: Map[RaceId, Race] = Map()

  implicit val interpreter = new RunnerAlg[Task] with RaceAlg[Task] {
    override def saveRunner(runner: Runner): Task[Unit] = {
      runnerStore = runnerStore.updated(runner.id, runner)
      Task(())
    }

    override def findRunner(id: RunnerId): Task[Option[Runner]] = Task(runnerStore.get(id))

    override def saveRace(race: Race): Task[Unit] = {
      raceStore = raceStore.updated(race.id, race)
      Task(())
    }

    override def findRace(id: RaceId): Task[Option[Race]] = Task(raceStore.get(id))
  }

  override def server(args: List[String]): Task[Server] = BlazeBuilder
    .bindHttp(port = 8080, host = "localhost")
    .mountService(RaceService.service, "/")
    .start
}
