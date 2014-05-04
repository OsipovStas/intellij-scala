package org.jetbrains.plugins.scala.dsl.types

import org.jetbrains.plugins.scala.dsl.tree.{Member, TypedMember}
import scala.language.implicitConversions
import scala.NoSuchElementException

/**
 * @author stasstels
 * @since  4/29/14.
 */


sealed trait Type extends ((TypedMember) => ScalaType)

trait Context {

  val shouldResolveAnnotation: Boolean

  def member: Member

  def equiv(st1: ScalaType, st2: ScalaType): Boolean

  def conform(st1: ScalaType, st2: ScalaType): Boolean

}


sealed trait ScalaType extends Type {


  override def apply(v1: TypedMember): ScalaType = this

  def =:=(that: ScalaType)(implicit ctx: Context): Boolean = ctx.equiv(this, that)

  def <:<(that: ScalaType)(implicit ctx: Context): Boolean = ???

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

  object Any extends StdTypeImpl("Any")

  object AnyVal extends StdTypeImpl("AnyVal")

  object Nothing extends StdTypeImpl("Nothing")

  object Int extends StdTypeImpl("Int")

  object Boolean extends StdTypeImpl("Boolean")

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

sealed abstract class FunList extends Iterable[ScalaType] {

  def h: ScalaType

  def t: FunList

  def isEmpty: Boolean

  def =>:(x: ScalaType) = new =>:(x, this)


  override def iterator: Iterator[ScalaType] = new Iterator[ScalaType] {
    override def hasNext: Boolean = !isEmpty

    override def next(): ScalaType = h
  }

}

case object Nil extends FunList {

  override def h: ScalaType = throw new NoSuchElementException

  override def t: FunList = throw new NoSuchElementException

  override def isEmpty: Boolean = true
}

case class =>:(h: ScalaType, t: FunList) extends FunList {
  override def isEmpty: Boolean = false
}

object FunList {

  implicit def list2type(xs: FunList): ScalaType = {
    val types = xs.toList
    val designatorType = DesignatorType(s"scala.Function${types.length - 1}")
    ParametrizedType(designatorType, types)
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