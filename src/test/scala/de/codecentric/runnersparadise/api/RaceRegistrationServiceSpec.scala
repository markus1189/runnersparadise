package de.codecentric
package runnersparadise.api

import java.util.concurrent.atomic.AtomicReference

import de.codecentric.runnersparadise.domain._
import de.codecentric.runnersparadise.fixtures.{RaceFixtures, RunnerFixtures}
import de.codecentric.runnersparadise.interpreters.{Pure, PureState}
import io.circe.Json
import org.http4s._
import org.http4s.circe._

import scalaz.concurrent.Task
import scalaz.~>

class RaceRegistrationServiceSpec extends UnitSpec {
  "RaceRegistrationService" should {
    "get present runners" in new WithFixtures {
      val req = Request(method = Method.GET, uri = Uri(path = s"/runner/${runner.id.value}"))

      val resp = performRequest(req)

      resp.status should ===(Status.Ok)
    }

    "return NotFound if trying to get absent runner" in new WithFixtures {
      val requestedRunnerId = RunnerId.random()
      val req =
        Request(method = Method.GET, uri = Uri(path = s"/runner/${requestedRunnerId.value}"))

      val resp   = performRequest(req)
      val result = resp.as(EntityDecoder.text).run

      resp.status should ===(Status.NotFound)
      result should ===(RaceRegistrationService.messages.noSuchRunner(requestedRunnerId))
    }

    "create new runners" in new WithFixtures {
      val firstname = "the-firstname"
      val lastname  = "the-lastname"

      val req = Request(method = Method.POST, uri = Uri(path = s"/runner"))
        .withBody(Json.obj("firstname" -> Json.fromString(firstname),
                           "lastname"  -> Json.fromString(lastname)))
        .run

      val resp = performRequest(req)
      resp.status should ===(Status.Created)

      val result = resp.as(jsonOf[Runner]).run
      result.firstname should ===(firstname)
      result.lastname should ===(lastname)

      state.get.runners.get(result.id).value should ===(result)
    }

    "get present races" in new WithFixtures {
      val req = Request(method = Method.GET, uri = Uri(path = s"/race/${race.id.value}"))

      val resp = performRequest(req)

      resp.status should ===(Status.Ok)
    }

    "return NotFound if trying to get absent race" in new WithFixtures {
      val requestedRaceId = RaceId.random()
      val req =
        Request(method = Method.GET, uri = Uri(path = s"/race/${requestedRaceId.value}"))

      val resp = performRequest(req)
      resp.status should ===(Status.NotFound)

      val result = resp.as(EntityDecoder.text).run
      result should ===(RaceRegistrationService.messages.noSuchRace(requestedRaceId))
    }

    "create new races" in new WithFixtures {
      val name = "Forever Alone Race"
      val max  = 1L

      val req = Request(method = Method.POST, uri = Uri(path = "/race"))
        .withBody(Json.obj("name" -> Json.fromString(name), "maxAttendees" -> Json.fromLong(max)))
        .run

      val resp = performRequest(req)
      resp.status should ===(Status.Created)

      val result = resp.as(jsonOf[Race]).run
      result.name should ===(name)
      result.maxAttendees should ===(max)

      state.get.races.get(result.id).value should ===(result)
    }

    "create a registration for the first attendee" in new WithFixtures {
      val req = Request(method = Method.PUT, uri = Uri(path = "/registration"))
        .withBody(Json.obj("runner" -> Json.fromString(runner.id.value.shows),
                           "race"   -> Json.fromString(race.id.value.shows)))
        .run

      val resp = performRequest(req)
      resp.status should ===(Status.Created)

      val result = resp.as(jsonOf[Registration]).run
      result.race should ===(race)
      result.attendees.headOption.value should ===(runner)
    }

    "update an existing registration for another attendee if enough free places left" in new WithFixtures {
      val newRunner = RunnerFixtures.create(firstname = "New Kid", lastname = "On the block")
      import Pure.AtomicReferenceOps

      state.update(
        s =>
          s.copy(registrations = s.registrations.updated(race.id, Registration(race, Set(runner))),
                 runners = s.runners.updated(newRunner.id, newRunner)))

      val req = Request(method = Method.PUT, uri = Uri(path = "/registration"))
        .withBody(Json.obj("runner" -> Json.fromString(newRunner.id.value.shows),
                           "race"   -> Json.fromString(race.id.value.shows)))
        .run

      val resp = performRequest(req)
      resp.status should ===(Status.Ok)

      val result = resp.as(jsonOf[Registration]).run
      result should ===(Registration(race, Set(newRunner, runner)))
    }

    "reject registration if there are no places left" in new WithFixtures {
      import Pure.AtomicReferenceOps
      val newRace = RaceFixtures.create(name = "Very Exclusive Race", maxAttendees = 0)

      state.update(s => s.copy(races = s.races.updated(newRace.id, newRace)))

      val req = Request(method = Method.PUT, uri = Uri(path = "/registration"))
        .withBody(Json.obj("runner" -> Json.fromString(runner.id.value.shows),
                           "race"   -> Json.fromString(newRace.id.value.shows)))
        .run

      val resp = performRequest(req)
      resp.status should ===(Status.BadRequest)

      val result = resp.as(EntityDecoder.text).run
      result should ===(RaceRegistrationService.messages.raceHasMaxAttendees)
    }

  }

  trait WithFixtures {
    val runner       = RunnerFixtures.harryDesden
    val race         = RaceFixtures.runnersParadise
    val initialState = PureState(Map(runner.id -> runner), Map(race.id -> race), Map.empty)

    val state = new AtomicReference(initialState)

    val registrationService = new RaceRegistrationService(new (Pure ~> Task) {
      override def apply[A](fa: Pure[A]): Task[A] = Task.delay(fa.value(state))
    })

    def performRequest(req: Request): Response = registrationService.service.run(req).run

    def run[A](fa: Pure[A]): A = fa.value(state)
  }

}
