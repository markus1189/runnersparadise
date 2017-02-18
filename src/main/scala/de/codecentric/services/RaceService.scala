package de.codecentric
package services

import de.codecentric.domain.{Race, RaceId, RunnerId}
import de.codecentric.persistence.{RaceAlg, RegistrationAlg, RunnerAlg, RunnerFunctions}
import de.codecentric.programs.Programs
import io.circe.Decoder
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._
import org.http4s.headers.Location

import scalaz.concurrent.Task
import scalaz.syntax.apply._

class RaceService(implicit A: RunnerAlg[Task], B: RaceAlg[Task], C: RegistrationAlg[Task]) {
  import RaceService._

  def service: HttpService = {
    def addRace(request: Request): Task[Response] = {
      request.decodeWith(jsonOf[AddRace], strict = true) { r =>
        val raceId = RaceId.random()
        RaceAlg().saveRace(Race(raceId, r.name, r.maxAttendees)) *> Created().putHeaders(
          Location(Uri.fromString(s"/race/${raceId.value}").getOrElse(???)))
      }
    }

    def getRunner(rid: RunnerId): Task[Response] = {
      RunnerAlg().findRunner(rid).flatMap {
        case Some(runner) => Ok(runner.asJson)
        case None         => NotFound()
      }
    }

    def getRace(rid: RaceId): Task[Response] = {
      RaceAlg().findRace(rid).flatMap {
        case Some(race) => Ok(race.asJson)
        case None       => NotFound()
      }
    }

    def addRunner(request: Request): Task[Response] = {
      request.decodeWith(jsonOf[AddRunner], strict = true) { r =>
        val runner = RunnerFunctions.createRunner(r)
        RunnerAlg().saveRunner(runner) *> Created().putHeaders(
          Location(Uri.fromString(s"/runner/${runner.id.value}").getOrElse(???)))
      }
    }

    def registerRunnerForRace(request: Request): Task[Response] = {
      request.decodeWith(jsonOf[Registration], strict = true) { registration =>
        Programs.register[Task](registration.runner, registration.race).flatMap {
          case Right(()) => Ok()
          case Left(msg) => BadRequest(msg)
        }
      }
    }

    HttpService {
      case GET -> Root / "runner" / RunnerIdVar(rid) => getRunner(rid)
      case GET -> Root / "race" / RaceIdVar(rid)     => getRace(rid)
      case request @ PUT -> Root / "registration"    => registerRunnerForRace(request)
      case request @ POST -> Root / "runner"         => addRunner(request)
      case request @ POST -> Root / "race"           => addRace(request)
    }
  }

}

object RaceService {
  object RunnerIdVar {
    def unapply(str: String): Option[RunnerId] = Some(RunnerId(str))
  }

  object RaceIdVar {
    def unapply(str: String): Option[RaceId] = Some(RaceId(str))
  }

  case class AddRunner(firstname: String, lastname: String, nickname: Option[String])

  object AddRunner {
    implicit val decoder: Decoder[AddRunner] = io.circe.generic.semiauto.deriveDecoder
  }

  case class AddRace(name: String, maxAttendees: Long)

  object AddRace {
    implicit val decoder: Decoder[AddRace] = io.circe.generic.semiauto.deriveDecoder
  }

  case class Registration(runner: RunnerId, race: RaceId)

  object Registration {
    implicit val decoder: Decoder[Registration] = io.circe.generic.semiauto.deriveDecoder
  }
}
