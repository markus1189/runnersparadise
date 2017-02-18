package de.codecentric
package runnersparadise.domain

import java.util.UUID

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

import scalaz.Equal

case class RaceId(value: String) extends AnyVal

object RaceId {
  implicit val eq: Equal[RaceId] = Equal.equalA
  implicit val raceIdDecoder: Decoder[RaceId] = Decoder[String].map(RaceId(_))
  implicit val raceIdEncoder: Encoder[RaceId] =
    Encoder.encodeString.contramap(_.value)

  def random(): RaceId = RaceId(UUID.randomUUID().toString)
}

case class Race(id: RaceId, name: String, maxAttendees: Long)

object Race {
  implicit val decoder: Decoder[Race] = deriveDecoder
  implicit val encoder: Encoder[Race] = deriveEncoder
}
