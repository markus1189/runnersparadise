package de.codecentric.runnersparadise.api

import java.util.UUID

import de.codecentric.runnersparadise.Errors.RegistrationError._
import de.codecentric.runnersparadise.algebra.{RaceAlg, RegistrationAlg, RunnerAlg, RunnerFunctions}
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
import scalaz.{Monad, \/, ~>}

class RaceRegistrationService[F[_]: Monad: RunnerAlg: RaceAlg: RegistrationAlg](toTask: F ~> Task) {

  import RaceRegistrationService._

  def service: HttpService = {
    def route = HttpService {
      case GET -> Root / "about"                         => handleAbout()
      case GET -> Root / "runner" / RunnerIdVar(rid)     => handleGetRunner(rid)
      case GET -> Root / "race" / RaceIdVar(rid)         => handleGetRace(rid)
      case GET -> Root / "registration" / RaceIdVar(rid) => handleGetRegistration(rid)
      case req @ PUT -> Root / "registration"            => handleRegistration(req)
      case req @ POST -> Root / "runner"                 => handleAddRunner(req)
      case req @ POST -> Root / "race"                   => handleAddRace(req)
    }

    def handleAddRace(request: Request): Task[Response] = {
      request.decodeWith(jsonOf[AddRace], strict = true) { r =>
        val raceId = RaceId.random()
        val race   = Race(raceId, r.name, r.maxAttendees)
        toTask(RaceAlg().saveRace(race)) *>
          Uri
            .fromString(s"/race/${raceId.value}")
            .map(uri => Created(race.asJson).putHeaders(Location(uri)))
            .getOrElse(InternalServerError())
      }
    }

    def handleAbout(): Task[Response] = Ok(messages.about)

    def handleGetRunner(rid: RunnerId): Task[Response] = {
      toTask(RunnerAlg().findRunner(rid)).flatMap {
        case Some(runner) => Ok(runner.asJson)
        case None         => NotFound(messages.noSuchRunner(rid))
      }
    }

    def handleGetRace(rid: RaceId): Task[Response] = {
      toTask(RaceAlg().findRace(rid)).flatMap {
        case Some(race) => Ok(race.asJson)
        case None       => NotFound(messages.noSuchRace(rid))
      }
    }

    def handleAddRunner(request: Request): Task[Response] = {
      request.decodeWith(jsonOf[AddRunner], strict = true) { r =>
        val runner = RunnerFunctions.createRunner(r)
        toTask(RunnerAlg().saveRunner(runner)) *>
          Uri
            .fromString(s"/runner/${runner.id.value}")
            .map(uri => Created(runner.asJson).putHeaders(Location(uri)))
            .getOrElse(InternalServerError())
      }
    }

    def handleRegistration(request: Request): Task[Response] = {
      request.decodeWith(jsonOf[Register], strict = true) { registration =>
        toTask(Programs.register[F](registration.runner, registration.race)).flatMap {
          case Right(reg) =>
            if (reg.attendees.size == 1) {
              Uri
                .fromString(s"/registration/${reg.race.id.value}")
                .map(uri => Created(reg.asJson).putHeaders(Location(uri)))
                .getOrElse(InternalServerError())
            } else {
              Ok(reg.asJson)
            }
          case Left(e) =>
            e match {
              case RaceNotFound(id)          => BadRequest(messages.registrationNoSuchRace(id))
              case RunnerNotFound(id)        => BadRequest(messages.noSuchRunner(id))
              case RegistrationNotFound(id)  => BadRequest(messages.registrationNoSuchRace(id))
              case RegistrationSaveFailed(_) => InternalServerError()
              case RaceHasMaxAttendees       => BadRequest(messages.raceHasMaxAttendees)
            }
        }
      }
    }

    def handleGetRegistration(raceId: RaceId): Task[Response] = {
      toTask(RegistrationAlg().findReg(raceId)).flatMap {
        case Some(reg) => Ok(reg.asJson)
        case None      => NotFound(messages.registrationNotFound(raceId))
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
    val raceHasMaxAttendees: String              = "No further registrations allowed for this race"
    val about: String                            = "This is a race registration service for runners."
    def registrationNotFound(id: RaceId): String = s"No registration exists for race ${id.value}"
  }

  object RunnerIdVar {
    def unapply(str: String): Option[RunnerId] =
      \/.fromTryCatchNonFatal(RunnerId(UUID.fromString(str))).toOption
  }

  object RaceIdVar {
    def unapply(str: String): Option[RaceId] =
      \/.fromTryCatchNonFatal(RaceId(UUID.fromString(str))).toOption
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
