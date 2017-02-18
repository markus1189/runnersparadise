package de.codecentric
package runnersparadise.domain

import java.util.UUID

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

case class RunnerId(value: String) extends AnyVal

object RunnerId {
  implicit val runnerIdDecoder: Decoder[RunnerId] = Decoder[String].map(RunnerId(_))
  implicit val runnerIdEncoder: Encoder[RunnerId] = Encoder.encodeString.contramap(_.value)

  def random(): RunnerId = RunnerId(UUID.randomUUID().toString)
}

case class Runner(id: RunnerId, firstname: String, lastname: String, nickname: Option[String])

object Runner {
  implicit val decoder: Decoder[Runner] = deriveDecoder
  implicit val encoder: Encoder[Runner] = deriveEncoder
}
