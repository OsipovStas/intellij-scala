package org.jetbrains.plugins.scala.dsl.types

import org.jetbrains.plugins.scala.dsl.types.FunList.AbstractIterator

/**
 * @author stasstels
 * @since  5/5/14.
 */
sealed abstract class FunList extends Iterable[ScalaType] {
  self =>

  def h: ScalaType

  def t: FunList

  def isEmpty: Boolean

  def =>:(x: ScalaType) = new =>:(x, this)


  override def iterator: Iterator[ScalaType] =  new AbstractIterator {
    var these = self
    def hasNext: Boolean = !these.isEmpty
    def next(): ScalaType =
      if (hasNext) {
        val result = these.h
        these = these.t
        result
      } else Iterator.empty.next()


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

  private abstract class AbstractIterator extends Iterator[ScalaType]


}