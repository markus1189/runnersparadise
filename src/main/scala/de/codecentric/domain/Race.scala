package de.codecentric.domain

case class RaceId(value: String) extends AnyVal

final case class Race(id: RaceId, name: String, attendees: Vector[Runner], maxAttendees: Long) {
  val freePlaces: Long = maxAttendees - attendees.size

  def register(runner: Runner): Option[Race] = {
    if (freePlaces > 0) {
      Some(copy(attendees = attendees :+ runner))
    } else {
      None
    }
  }
}