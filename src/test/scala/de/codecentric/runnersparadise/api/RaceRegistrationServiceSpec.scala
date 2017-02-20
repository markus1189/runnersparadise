package de.codecentric
package runnersparadise.api

import de.codecentric.runnersparadise.domain._
import de.codecentric.runnersparadise.fixtures.{RaceFixtures, RunnerFixtures}
import de.codecentric.runnersparadise.interpreters.InMemoryInterpreters
import io.circe.Json
import org.http4s._
import org.http4s.circe._

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

      interpreters.runnerStore.get(result.id).value should ===(result)
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

      interpreters.raceStore.get(result.id).value should ===(result)
    }

    "complain if trying to create a registration for non existant race" in new WithFixtures {
      val randomRaceId = RaceId.random()
      val req = Request(method = Method.POST, uri = Uri(path = "/registration"))
        .withBody(Json.obj("race" -> Json.fromString(randomRaceId.value.shows)))
        .run

      val resp = performRequest(req)
      resp.status should ===(Status.BadRequest)

      val result = resp.as(EntityDecoder.text).run
      result should ===(RaceRegistrationService.messages.registrationNoSuchRace(randomRaceId))
    }

    "create new registrations for an existing race" in new WithFixtures {
      val req = Request(method = Method.POST, uri = Uri(path = "/registration"))
        .withBody(Json.obj("race" -> Json.fromString(race.id.value.shows)))
        .run

      val resp = performRequest(req)
      resp.status should ===(Status.Created)

      val result = resp.as(jsonOf[Registration]).run
      result.race.id should ===(race.id)

      interpreters.regStore.get(race.id).value should ===(result)
    }

    "fail to create registration if race is unknown" in new WithFixtures {
      val randomRaceId = RaceId.random()

      val req = Request(method = Method.POST, uri = Uri(path = "/registration"))
        .withBody(Json.obj("race" -> Json.fromString(randomRaceId.value.shows)))
        .run

      val resp = performRequest(req)
      resp.status should ===(Status.BadRequest)

      val result = resp.as(EntityDecoder.text).run
      result should ===(RaceRegistrationService.messages.registrationNoSuchRace(randomRaceId))
    }

    "creates a new registration for an existing race" in new WithFixtures {
      val req = Request(method = Method.POST, uri = Uri(path = "/registration"))
        .withBody(Json.obj("race" -> Json.fromString(race.id.value.shows)))
        .run

      val resp = performRequest(req)
      resp.status should ===(Status.Created)

      val result = resp.as(jsonOf[Registration]).run
      result should ===(Registration(race, Set()))
    }
  }

  trait WithFixtures {
    val runner              = RunnerFixtures.harryDesden
    val race                = RaceFixtures.runnersParadise
    val interpreters        = new InMemoryInterpreters
    import interpreters._
    val registrationService = new RaceRegistrationService

    interpreters.runners.saveRunner(runner)
    interpreters.races.saveRace(race)

    def performRequest(req: Request): Response = registrationService.service.run(req).run
  }

}
