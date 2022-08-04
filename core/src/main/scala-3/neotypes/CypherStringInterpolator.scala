package neotypes

import neotypes.DeferredQueryBuilder
import neotypes.mappers.QueryArgMapper
import scala.quoted.Expr
import scala.quoted.Quotes
import scala.quoted.Varargs

class CypherStringInterpolator(sc: StringContext) {
  inline def c(inline args: Any*): DeferredQueryBuilder = ${ CypherStringInterpolator.macroImpl('this, 'args) }
}


object CypherStringInterpolator {
  // https://tersesystems.com/blog/2021/07/19/adding-scala-3-support-to-blindsight/
  def macroImpl(interpolator: Expr[CypherStringInterpolator],
                args: Expr[Seq[Any]])(using q: Quotes): Expr[DeferredQueryBuilder] =
    import q.reflect.*

    val queries = interpolator.asTerm.underlyingArgument match {
      case Apply(_conv, List(Apply(_fun, List(Typed(Repeated(values, _), _))))) =>
        values.collect { case Literal(StringConstant(value)) => value }.iterator
    }

    val params = args.asTerm.underlyingArgument match {
      case Typed(Repeated(allArgs, _), _) => allArgs.iterator
    }

    @annotation.tailrec
    def loop(paramNext: Boolean, acc: List[Expr[Any]]): List[Expr[Any]] = {
      if (paramNext && params.hasNext) {
        val nextParam = params.next
        val tpe = nextParam.tpe.widen

        loop(
          paramNext = false,
          '{Right(QueryArgMapper[${tpe}].toArg(nextParam))} :: acc
        )
      } else if (queries.hasNext) {
        val Literal(StringConstant(query: String)) = queries.next()

        if (query.endsWith("#"))
          loop(
            paramNext = false,
            '{Left(params.next.toString)} :: '{Left(Expr(query.init))} :: acc
          )
        else
          loop(
            paramNext = true,
            '{Left(query)} :: acc
          )
      } else {
        acc.reverse
      }
    }

    loop(
      paramNext = false,
      acc = List.empty
    )

    '{_root_.neotypes.internal.CypherStringInterpolator.createQuery(Left(""))}
}
