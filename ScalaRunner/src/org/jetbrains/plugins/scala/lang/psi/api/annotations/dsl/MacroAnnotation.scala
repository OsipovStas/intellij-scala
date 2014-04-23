package org.jetbrains.plugins.scala.lang.psi.api.annotations.dsl

/**
 * @author stasstels
 * @since  3/30/14.
 */
object MacroAnnotation {

  type Ctx[T] = AnnotationHolder => T

  type OptCtx[T] = Ctx[Option[T]]

  implicit def lift[T](arg: T): Ctx[T] = _ => arg

  implicit class RichOptCtx[T <: AnnotationHolder](val ctxValue: OptCtx[T]) extends AnyVal {

    def asValue: OptCtx[ValueHolder] = ctxValue andThen (_.flatMap {
      case v: ValueHolder => Some(v)
      case _ => None
    })


    def asVariable: OptCtx[VarHolder] = ctxValue andThen (_.flatMap {
      case v: VarHolder => Some(v)
      case _ => None
    })


    def getContainingClass: OptCtx[DefinitionsHolder] = ctxValue andThen (_.flatMap(_.getContainingClass))

  }

  implicit class RichDefCtx(val holder: OptCtx[DefinitionsHolder]) extends AnyVal {

    def +=(m: Method): SyntheticDefinitions = SyntheticDefinitions(holder, Seq(m))

    def ++(ms: Seq[Method]): SyntheticDefinitions = SyntheticDefinitions(holder, ms)


  }

  case class Method(name: String => String, params: Ctx[Seq[String]], returnType: Ctx[String])

  case class SyntheticDefinitions(owner: OptCtx[DefinitionsHolder], members: Seq[Method])

  object holder extends OptCtx[AnnotationHolder] {

    override def apply(v1: AnnotationHolder): Option[AnnotationHolder] = Some(v1)

    def filter(f: AnnotationHolder => Boolean): OptCtx[AnnotationHolder] = {
      case holder if f(holder) => Some(holder)
      case _ => None
    }

  }

  object Methods {

    val getter = Method(
      name = "get" + _.capitalize,
      params = h => Seq(),
      returnType = _.getType
    )

    val isGetter = Method(
      name = "is" + _.capitalize,
      params = h => Seq(),
      returnType = _.getType
    )

    val setter = Method(
      name = "set" + _.capitalize,
      params = h => Seq(h.getType),
      returnType = n => "Unit"
    )

  }


}

class MacroAnnotation {

  import MacroAnnotation._

  var definitions: Seq[SyntheticDefinitions] = Seq()

  var annotations: Seq[String] = Seq()

}
