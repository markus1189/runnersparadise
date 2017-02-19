package de.codecentric.runnersparadise.fixtures

import de.codecentric.runnersparadise.domain.{Race, RaceId}

object RaceFixtures {
  val runnersParadise = create(name = "Runner's Paradise Challenge", maxAttendees = 42)

  def create(id: RaceId = RaceId.random(),
             name: String = "no-race-name",
             maxAttendees: Long = 0): Race = {
    Race(id, name, maxAttendees)
  }
}
