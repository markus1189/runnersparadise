package de.codecentric
package services

import de.codecentric.domain.{Race, RaceId, Runner, RunnerId}
import de.codecentric.persistence.{RaceAlg, RunnerAlg}
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._
import org.http4s.headers.Location

import scalaz.concurrent.Task
import scalaz.syntax.apply._

object RaceService {
  def service(implicit A: RunnerAlg[Task] with RaceAlg[Task]) = HttpService {
    case GET -> Root / "runner" / RunnerIdVar(rid) =>
      RunnerAlg().findRunner(rid).flatMap {
        case Some(runner) => Ok(runner.asJson)
        case None         => NotFound()
      }
    case GET -> Root / "runner" / RunnerIdVar(RunnerId("6")) =>
      Ok(Runner(RunnerId("6"), "Runner", "6", None).asJson)
    case GET -> Root / "race" / RaceIdVar(rid) =>
      RaceAlg().findRace(rid).flatMap {
        case Some(race) => Ok(race.asJson)
        case None       => NotFound()
      }
    case request @ POST -> Root / "runner" =>
      request.decodeWith(jsonOf[Runner], strict = true) { r =>
        RunnerAlg().saveRunner(r) *> Created().putHeaders(
          Location(Uri.fromString(s"/runner/${r.id.value}").getOrElse(???)))
      }
    case request @ POST -> Root / "race" =>
      request.decodeWith(jsonOf[Race], strict = true) { r =>
        RaceAlg().saveRace(r) *> Created().putHeaders(
          Location(Uri.fromString(s"/race/${r.id.value}").getOrElse(???)))
      }
  }
}

object RunnerIdVar {
  def unapply(str: String): Option[RunnerId] = Some(RunnerId(str))
}

object RaceIdVar {
  def unapply(str: String): Option[RaceId] = Some(RaceId(str))
}
