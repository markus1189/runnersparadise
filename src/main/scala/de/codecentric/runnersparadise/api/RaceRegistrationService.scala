package de.codecentric.runnersparadise.api

import de.codecentric.runnersparadise.algebra.{
  RaceAlg,
  RegistrationAlg,
  RunnerAlg,
  RunnerFunctions
}
import de.codecentric.runnersparadise.domain.{Race, RaceId, RunnerId}
import de.codecentric.runnersparadise.programs.Programs
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._
import org.http4s.headers.Location

import scalaz.concurrent.Task
import scalaz.syntax.apply._

class RaceRegistrationService(implicit A: RunnerAlg[Task],
                              B: RaceAlg[Task],
                              C: RegistrationAlg[Task]) {

  import RaceRegistrationService._

  def service: HttpService = {
    def route = HttpService {
      case GET -> Root / "about"                           => handleAbout()
      case GET -> Root / "runner" / RunnerIdVar(rid)       => handleGetRunner(rid)
      case GET -> Root / "race" / RaceIdVar(rid)           => handleGetRace(rid)
      case GET -> Root / "registration" / RaceIdVar(rid)   => handleGetRegistration(rid)
      case request @ POST -> Root / "registration" / "new" => handleNewRegistration(request)
      case request @ PUT -> Root / "registration" / "add"  => handleRegistration(request)
      case request @ POST -> Root / "runner"               => handleAddRunner(request)
      case request @ POST -> Root / "race"                 => handleAddRace(request)
    }

    def handleAddRace(request: Request): Task[Response] = {
      request.decodeWith(jsonOf[AddRace], strict = true) { r =>
        val raceId = RaceId.random()
        val race   = Race(raceId, r.name, r.maxAttendees)
        RaceAlg().saveRace(race) *> Created(race.asJson)
          .putHeaders(Location(Uri.fromString(s"/race/${raceId.value}").getOrElse(???)))
      }
    }

    def handleAbout(): Task[Response] = Ok("This is a race registration service for runners.")

    def handleGetRunner(rid: RunnerId): Task[Response] = {
      RunnerAlg().findRunner(rid).flatMap {
        case Some(runner) => Ok(runner.asJson)
        case None         => NotFound(messages.noSuchRunner(rid))
      }
    }

    def handleGetRace(rid: RaceId): Task[Response] = {
      RaceAlg().findRace(rid).flatMap {
        case Some(race) => Ok(race.asJson)
        case None       => NotFound(messages.noSuchRace(rid))
      }
    }

    def handleAddRunner(request: Request): Task[Response] = {
      request.decodeWith(jsonOf[AddRunner], strict = true) { r =>
        val runner = RunnerFunctions.createRunner(r)
        RunnerAlg().saveRunner(runner) *> Created(runner.asJson)
          .putHeaders(Location(Uri.fromString(s"/runner/${runner.id.value}").getOrElse(???)))
      }
    }

    def handleRegistration(request: Request): Task[Response] = {
      request.decodeWith(jsonOf[Register], strict = true) { registration =>
        Programs.register[Task](registration.runner, registration.race).flatMap {
          case Right(reg) => Ok(reg.asJson)
          case Left(msg)  => BadRequest(msg)
        }
      }
    }

    def handleNewRegistration(request: Request): Task[Response] = {
      request.decodeWith(jsonOf[AddRegistration], strict = true) {
        case AddRegistration(raceId) =>
          RegistrationAlg().newReg(raceId).flatMap {
            case Some(registration) => Created(registration.asJson)
            case None               => BadRequest(messages.registrationNoSuchRace(raceId))
          }
      }

    }

    def handleGetRegistration(raceId: RaceId): Task[Response] = {
      RegistrationAlg().findReg(raceId).flatMap {
        case Some(reg) => Ok(reg.asJson)
        case None      => NotFound(s"No registration exists for race $raceId")
      }
    }

    route
  }

}

object RaceRegistrationService {
  object messages {
    def noSuchRunner(id: RunnerId): String = s"No such runner: ${id.value}"
    def noSuchRace(id: RaceId): String     = s"No such race: ${id.value}"
    def registrationNoSuchRace(id: RaceId): String =
      s"Cannot create registration for unknown race: ${id.value}"
  }

  object RunnerIdVar {
    def unapply(str: String): Option[RunnerId] = Some(RunnerId(str))
  }

  object RaceIdVar {
    def unapply(str: String): Option[RaceId] = Some(RaceId(str))
  }

  case class AddRunner(firstname: String, lastname: String, nickname: Option[String])

  object AddRunner {
    implicit val decoder: Decoder[AddRunner] = deriveDecoder
  }

  case class AddRace(name: String, maxAttendees: Long)

  object AddRace {
    implicit val decoder: Decoder[AddRace] = deriveDecoder
  }

  case class Register(runner: RunnerId, race: RaceId)

  object Register {
    implicit val decoder: Decoder[Register] = deriveDecoder
  }

  case class AddRegistration(race: RaceId)

  object AddRegistration {
    implicit val decoder: Decoder[AddRegistration] = deriveDecoder
  }

}
