package de.codecentric.domain

case class RunnerId(value: String) extends AnyVal

case class Runner(id: RunnerId, firstname: String, lastname: String, nickname: Option[String])
