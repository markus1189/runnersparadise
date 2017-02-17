package de.codecentric.domain

import io.circe.{Decoder, Encoder, Json}

case class RunnerId(value: String) extends AnyVal

object RunnerId {
  implicit val runnerIdDecoder: Decoder[RunnerId] = Decoder[String].map(RunnerId(_))
  implicit val runnerIdEncoder: Encoder[RunnerId] = Encoder.encodeString.contramap(_.value)
}

case class Runner(id: RunnerId, firstname: String, lastname: String, nickname: Option[String])

object Runner {
  implicit val decoder: Decoder[Runner] = Decoder.forProduct4("id", "firstname", "lastname", "nickname")(Runner.apply)
  implicit val encoder: Encoder[Runner] = (a: Runner) => Json.obj(
    "id" -> Encoder[RunnerId](implicitly)(a.id),
    "firstname" -> Json.fromString(a.firstname),
    "lastname" -> Json.fromString(a.lastname),
    "nickname" -> a.nickname.map(Json.fromString).getOrElse(Json.Null)
  )
}
