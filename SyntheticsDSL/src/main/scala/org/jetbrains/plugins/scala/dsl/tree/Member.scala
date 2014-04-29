package org.jetbrains.plugins.scala.dsl.tree

import org.jetbrains.plugins.scala.dsl.{Context, TypeContext}

/**
 * @author stasstels
 * @since  4/29/14.
 */
trait Member extends AnnotationHolder with Named {

  override def hasAnnotation(a: Annotation): Boolean = false

  def isValue: Boolean = false

  def isVariable = false

  def asValue: Value = None

  def asVariable: Variable = None

  def containingClass: ScalaClass

  def typecheck(p: TypeContext => Boolean): Member = this
}


object Member {
  implicit def none2empty(t: None.type): Empty.type = Empty


  implicit def holder2option[H <: Member](h: H): Option[H] = h match {
    case Empty => None
    case _ => Some(h)
  }

  def fromContext(implicit ctx: Context): Member = {
    ctx.member
  }

}