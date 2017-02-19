package de.codecentric
package runnersparadise.domain

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

case class Registration(race: Race, attendees: Set[Runner]) {
  val freePlaces: Long = race.maxAttendees - attendees.size

  def add(runner: Runner): Option[Registration] = {
    if (freePlaces > 0) {
      Some(copy(attendees = attendees + runner))
    } else {
      None
    }
  }
}

object Registration {
  implicit val encoder: Encoder[Registration] = deriveEncoder
  implicit val decoder: Decoder[Registration] = deriveDecoder
}
