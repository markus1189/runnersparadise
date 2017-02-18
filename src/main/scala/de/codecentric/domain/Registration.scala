package de.codecentric.domain

import java.util.UUID

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

case class RegistrationId(value: String) extends AnyVal

object RegistrationId {
  def random() = RegistrationId(UUID.randomUUID().toString)

  implicit val encoder: Encoder[RegistrationId] = Encoder[String].contramap(_.value)
  implicit val decoder: Decoder[RegistrationId] = Decoder[String].map(RegistrationId(_))
}

case class Registration(id: RegistrationId, race: Race, attendees: Vector[Runner]) {
  val freePlaces: Long = race.maxAttendees - attendees.size

  def add(runner: Runner): Option[Registration] = {
    if (freePlaces > 0) {
      Some(copy(attendees = attendees :+ runner))
    } else {
      None
    }
  }
}

object Registration {
  implicit val encoder: Encoder[Registration] = deriveEncoder
  implicit val decoder: Decoder[Registration] = deriveDecoder
}
