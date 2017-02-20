package de.codecentric
package runnersparadise.domain

import java.util.UUID

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

case class RunnerId(value: UUID) extends AnyVal

object RunnerId {
  implicit val runnerIdDecoder: Decoder[RunnerId] = Decoder[String].map(s => RunnerId(UUID.fromString(s)))
  implicit val runnerIdEncoder: Encoder[RunnerId] = Encoder.encodeString.contramap(_.value.shows)

  def random(): RunnerId = RunnerId(UUID.randomUUID())
}

case class Runner(id: RunnerId, firstname: String, lastname: String, nickname: Option[String])

object Runner {
  implicit val decoder: Decoder[Runner] = deriveDecoder
  implicit val encoder: Encoder[Runner] = deriveEncoder
}
