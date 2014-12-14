package medusa

import scala.reflect.macros.whitebox.Context
import scala.language.experimental.macros
import scala.annotation.StaticAnnotation

/**
  * Known issues:
  * 1. type params on macro annotation http://stackoverflow.com/questions/19791686/type-parameters-on-scala-macro-annotations
  * 1. call by name https://issues.scala-lang.org/browse/SI-5778
  */

/** TODO:
  * 1. All methods exposed
  * 1. Default updates
  * 1. Custom method exposed
  * 1. Custom wrapper body
  * 1. Inheritance
  */

class medusa[T] extends StaticAnnotation {
  def macroTransform(annottees: Any*) = macro MedusaMacro.impl
}

object MedusaMacro {
  def impl(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe.{Symbol => _, _}

    def abort(msg: String) = {
      c.abort(c.enclosingPosition, s"Can't generate Medusa: $msg")
    }
    //println(c.macroApplication)
    //println(c.universe.showRaw(c.macroApplication))
    val (seedTypeName, wrapperTypeName) = c.macroApplication match {
      case Apply(
        Select(
          Apply(
            Select(
              New(
                AppliedTypeTree(
                  Ident(TypeName("medusa")),
                  List(Ident(seedTypeName))
                )
              ),
              termNames.CONSTRUCTOR
            ),
            List()
          ),
          TermName("macroTransform")
        ),
        List(
          //wrapperClasDef
          ClassDef(
            Modifiers(_, _, _),
            wrapperTypeName,
            List(),
            Template(
              parents,
              noSelfType,
              members
            )
          )
        )
      ) => (seedTypeName, wrapperTypeName)
      case _ => abort(
        "Sht happened."
      )
    }

    val TypeName(seedName) = seedTypeName
    val seedType = TypeName(seedName)

    val functype = AppliedTypeTree(
      Select(
        Select(Ident(termNames.ROOTPKG), "scala"),
        TypeName("Function1")
      ),
      List(
        Ident(seedType),
        Ident(TypeName("Unit"))
      )
    )

    c.Expr[Any](
      q"""
        class $wrapperTypeName(
          _seed: $seedType, val updates: List[$functype] = List()
        ) {
          private val seed = _seed

          def create(upds: List[$functype]) = 
            new $wrapperTypeName(seed, upds)
          
          def toMutant = 
            updates.foldRight(seed) {
              case (f, s) =>
                f(s)
                s
            }
        }
      """
      //val TypeName(wrapperName) = wrapperTypeName
      //object ${TermName(wrapperName)} {
      //  def apply(seed: $seedType): $wrapperTypeName = new $wrapperTypeName(seed)
      //}
    )
  }

}       
