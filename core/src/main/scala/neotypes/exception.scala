package neotypes

object exceptions {
  sealed abstract class NeotypesException(message: String, cause: Option[Throwable] = None) extends Exception(message, cause.orNull)

  case class PropertyNotFoundException(message: String) extends NeotypesException(message)

  case class IncoercibleException(message: String, cause: Option[Throwable] = None) extends NeotypesException(message, cause)

  case object TransactionWasNotCreatedException extends NeotypesException(message = "Couldn't create a transaction")

  case object CancellationException extends NeotypesException(message = "An operation was cancelled")
}
