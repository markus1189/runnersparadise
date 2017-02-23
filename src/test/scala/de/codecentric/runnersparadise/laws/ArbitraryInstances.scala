package de.codecentric.runnersparadise.laws

import de.codecentric.runnersparadise.domain.{Runner, RunnerId}
import org.scalacheck.{Arbitrary, Gen}

trait ArbitraryInstances {
  implicit val runnerArb: Arbitrary[Runner] = Arbitrary {
    for {
      id <- Gen.delay(RunnerId.random())
      firstname <- Gen.alphaStr
      lastname <- Gen.alphaStr
      nick <- Gen.option(Gen.alphaStr)
    } yield Runner(id,firstname,lastname,nick)
  }
}

object ArbitraryInstances extends ArbitraryInstances