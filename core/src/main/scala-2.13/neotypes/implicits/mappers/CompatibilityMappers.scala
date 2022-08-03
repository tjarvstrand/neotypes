package neotypes
package implicits.mappers
import shapeless.HNil

trait CompatibilityMappers {
  implicit final val HNilMapper: ValueMapper[HNil] =
    ValueMapper.const(HNil)
}
