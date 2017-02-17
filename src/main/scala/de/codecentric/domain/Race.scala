package de.codecentric.domain

import io.circe.{Decoder, Encoder, Json}

case class RaceId(value: String) extends AnyVal

object RaceId {
  implicit val raceIdDecoder: Decoder[RaceId] = Decoder[String].map(RaceId(_))
  implicit val raceIdEncoder: Encoder[RaceId] =
    Encoder.encodeString.contramap(_.value)
}

case class Race(id: RaceId, name: String, attendees: Vector[Runner], maxAttendees: Long) {
  val freePlaces: Long = maxAttendees - attendees.size

  def register(runner: Runner): Option[Race] = {
    if (freePlaces > 0) {
      Some(copy(attendees = attendees :+ runner))
    } else {
      None
    }
  }
}

object Race {
  implicit val decoder: Decoder[Race] =
    Decoder.forProduct4("id", "name", "attendees", "maxAttendees")(Race.apply)
  implicit val encoder: Encoder[Race] = (a: Race) =>
    Json.obj(
      "id"        -> Encoder[RaceId](implicitly)(a.id),
      "firstname" -> Json.fromString(a.name),
      "lastname"  -> Encoder[Vector[Runner]](implicitly)(a.attendees),
      "nickname"  -> Json.fromLong(a.maxAttendees)
  )
}
