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
                  List(seedTypeName)
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

    def getTypeNonBaseMethods(typeName: Tree) = {
      val seedTpe = c.typeCheck(q"(7.asInstanceOf[$typeName])").tpe
      val any = c.typeCheck(q"(7.asInstanceOf[Any])").tpe
      val obj = c.typeCheck(q"(7.asInstanceOf[Object])").tpe
      val baseMethods = Set(any, obj).flatMap(_.members.map(_.fullName).toSet)

      val methods = seedTpe.members.collect {
        case s: MethodSymbol if s.isPublic && !s.isAccessor && !s.isConstructor && !baseMethods.contains(s.fullName) => s
      }
      methods
    }

    val methods = getTypeNonBaseMethods(seedTypeName)
    val setters = methods.filter(_.returnType == typeOf[Unit])
    val getters = methods.filter(_.returnType != typeOf[Unit])
    
    val setterExprs = setters.map { s =>
      if (s.paramss.isEmpty || s.paramss.head.isEmpty) {
        q"""def ${s.name} = update(_.${s.name})"""
      } else {
        val names = s.paramss.head.zipWithIndex.map{ case (_, i) => newTermName("$" + i)}
        val params = s.paramss.head.zip(names).map {case (p, n) => q"""$n: ${p.typeSignature}"""}

        q"""
          def ${s.name}(..$params): $wrapperTypeName = update(_.${s.name}(..$names))
        """
      }
    }

    val getterExprs = getters.map { g =>
      if (g.paramss.isEmpty || g.paramss.head.isEmpty) {
        q"""def ${g.name} = toMutable.${g.name}"""
      } else {
        val names = g.paramss.head.zipWithIndex.map{ case (_, i) => newTermName("$" + i)}
        val params = g.paramss.head.zip(names).map {case (p, n) => q"""$n: ${p.typeSignature}"""}

        q"""
          def ${g.name}(..$params): ${g.returnType} = toMutable.${g.name}(..$names)
        """
      }
    }

    val functype = AppliedTypeTree(
      Select(
        Select(Ident(termNames.ROOTPKG), "scala"),
        TypeName("Function1")
      ),
      List(
        seedTypeName,
        Ident(TypeName("Unit"))
      )
    )

    c.Expr[Any](
      q"""
        class $wrapperTypeName(
          _seed: $seedTypeName, val updates: List[$functype] = List()
        ) {
          private val seed = _seed

          def update(f: $functype) = new $wrapperTypeName(seed, f :: updates)
          
          ..$setterExprs
          ..$getterExprs

          lazy val toMutable = 
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
