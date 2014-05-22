package org.jetbrains.plugins.scala.dsl.types

import org.jetbrains.plugins.scala.dsl.tree.{Template, TypedMember}
import scala.language.implicitConversions

/**
 * @author stasstels
 * @since  4/29/14.
 */



trait Context {

  def template: Template

  def equiv(st1: ScalaType, st2: ScalaType): Boolean

  def conform(st1: ScalaType, st2: ScalaType): Boolean

}


sealed trait ScalaType extends ((TypedMember) => ScalaType) {


  override def apply(v1: TypedMember): ScalaType = this

  def =:=(that: ScalaType)(implicit ctx: Context): Boolean = ctx.equiv(this, that)

  def <:<(that: ScalaType)(implicit ctx: Context): Boolean = ctx.conform(this, that)

  def show: String

  def =>:(st: ScalaType): FunList = new =>:(st, this =>: Nil)
}


object StdTypes {

  sealed class StdTypeImpl(name: String) {

    private object instance extends ScalaType {
      override def show: String = name
    }

    final def unapply(st: ScalaType)(implicit ctx: Context): Boolean = st =:= instance
  }

  object StdTypeImpl {
    implicit def std2type(std: StdTypeImpl): ScalaType = std.instance
  }

  object Any_ extends StdTypeImpl("scala.Any")

  object AnyVal_ extends StdTypeImpl("scala.AnyVal")

  object Nothing_ extends StdTypeImpl("scala.Nothing")

  object Int_ extends StdTypeImpl("Int")

  object Long_ extends StdTypeImpl("scala.Long")

  object Boolean_ extends StdTypeImpl("scala.Boolean")

  object Byte_ extends StdTypeImpl("scala.Byte")

  object Short_ extends StdTypeImpl("scala.Short")

  object Double_ extends StdTypeImpl("scala.Double")

  object Float_ extends StdTypeImpl("scala.Float")

  object Unit_ extends StdTypeImpl("scala.Unit")

  object Char_ extends StdTypeImpl("scala.Char")

  object String_ extends StdTypeImpl("String")

}


object DesignatorType {

  private case class DesignatorTypeImpl(name: String) extends ScalaType {
    override def show: String = name
  }

  def apply(name: String): ScalaType = DesignatorTypeImpl(name)

  def unapply(st: ScalaType)(implicit ctx: Context): Option[String] = {
    st match {
      case DesignatorTypeImpl(n) => Some(n)
      case _ => None
    }
  }


}

object ParametrizedType {

  private case class ParametrizedTypeImpl(designator: ScalaType, typeArgs: Seq[ScalaType]) extends ScalaType {
    override def show: String = {
      designator.show + typeArgs.map(_.show).mkString("[", ",", "]")
    }
  }

  def apply(designator: ScalaType, typeArgs: Seq[ScalaType]): ScalaType = ParametrizedTypeImpl(designator, typeArgs)

  def unapply(st: ScalaType): Option[(ScalaType, Seq[ScalaType])] = {
    st match {
      case ParametrizedTypeImpl(d, tps) => Some(d, tps)
      case _ => None
    }
  }


}

sealed abstract class SugarType(val designator: ScalaType)

object Types {


  object Seq_ extends SugarType(DesignatorType("scala.Seq")) {
    def apply(st: ScalaType): ScalaType = ParametrizedType(designator, Seq(st))
  }

  object Set_ extends SugarType(DesignatorType("scala.Set")) {
    def apply(st: ScalaType): ScalaType = ParametrizedType(designator, Seq(st))
  }

  object Map_ extends SugarType(DesignatorType("scala.Map")) {
    def apply(keyType: ScalaType, valueType: ScalaType): ScalaType = ParametrizedType(designator, Seq(keyType, valueType))
  }

  object Array_ extends SugarType(DesignatorType("scala.Array")) {
    def apply(st: ScalaType): ScalaType = ParametrizedType(designator, Seq(st))
  }

  object List_ extends SugarType(DesignatorType("scala.List")) {
    def apply(st: ScalaType): ScalaType = ParametrizedType(designator, Seq(st))
  }

}


object CompoundType {

  private case class CompoundTypeImpl(components: Seq[ScalaType], refinement: Refinement) extends ScalaType {
    override def show: String = {
      components.map(_.show).mkString(" with ") + refinement.show
    }
  }

  def apply(components: Seq[ScalaType], refinement: Refinement): ScalaType = CompoundTypeImpl(components, refinement)

  def unapply(st: ScalaType): Option[(Seq[ScalaType], Refinement)] = {
    st match {
      case CompoundTypeImpl(cmps, r) => Some(cmps, r)
      case _ => None
    }
  }


}

object SealedType {

  private case class SealedTypeImpl(present: String) extends ScalaType {
    override def show: String = present
  }


  def apply(presentation: String): ScalaType = SealedTypeImpl(presentation)


}