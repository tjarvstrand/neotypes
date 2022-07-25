package neotypes

case class Exported[+T](instance: T) extends AnyVal
