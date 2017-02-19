package de.codecentric.runnersparadise.fixtures

import de.codecentric.runnersparadise.domain.{Runner, RunnerId}

object RunnerFixtures {
  val harryDesden: Runner = create(id = RunnerId.random(), firstname = "Haryy", lastname = "Dresden")

  def create(id: RunnerId = RunnerId.random(),
             firstname: String = "no-first-name",
             lastname: String = "no-last-name",
             nickname: Option[String] = None): Runner = {
    Runner(id, firstname, lastname, nickname)
  }
}
