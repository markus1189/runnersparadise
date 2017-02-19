package de.codecentric.runnersparadise

import de.codecentric.runnersparadise.domain.{RaceId, RunnerId}

object Errors {
  sealed trait RegistrationError extends Product with Serializable
  object RegistrationError {
    case class RunnerNotFound(id: RunnerId)                 extends RegistrationError
    case class RegistrationNotFound(id: RaceId)             extends RegistrationError
    case class RegistrationSaveFailed(e: Option[Throwable]) extends RegistrationError
    case object RaceHasMaxAttendees                         extends RegistrationError
  }
}
