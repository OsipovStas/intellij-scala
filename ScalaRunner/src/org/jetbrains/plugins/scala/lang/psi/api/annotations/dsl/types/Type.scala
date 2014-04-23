package org.jetbrains.plugins.scala.lang.psi.api.annotations.dsl.types

/**
 * @author stasstels
 * @since  4/12/14.
 */
sealed trait Type {

  def presentation: String = this match {
    case SimpleType(c) => c
    case ParametrizedType(c, args) => c.presentation + args.map(_.presentation).mkString("[", ",", "]")
    case TupleType(types) => types.map(_.presentation).mkString("(", ",", ")")
    case FunctionType(x, y) => Seq(x.presentation, y.fold("")(_.presentation)).mkString("=>")
    case CompoundType(components) => components.mkString(" with ")
    case ParenthesisType(in) => "(" + in.fold("")(_.presentation) + ")"
    case InfixType(c, l, r) => l.presentation + s" $c " + r.fold("")(_.presentation)
    case ProjectionType(p, s) => p.presentation + "#" + s.presentation
  }

}

case class SimpleType(constructor: String) extends Type
case class ParametrizedType(constructor: Type, typeArgs: Seq[Type]) extends Type
case class TupleType(typeList: Seq[Type]) extends Type
case class FunctionType(paramType: Type, returnType: Option[Type]) extends Type
case class CompoundType(components: Seq[Type]) extends Type
case class ParenthesisType(inner: Option[Type]) extends Type
case class InfixType(constructor: String, left: Type, right: Option[Type]) extends Type
case class ProjectionType(prefix: Type, suffix: Type) extends Type