package org.jetbrains.plugins.scala.dsl.tree

import org.jetbrains.plugins.scala.dsl.types.{Context, ScalaType}

/**
 * @author stasstels
 * @since  4/29/14.
 */
trait Member extends AnnotationHolder {

  override def hasAnnotation(a: Annotation)(implicit ctx: Context): Boolean = false

  def isValue: Boolean = false

  def isVariable = false

  def asValue: Value = None

  def asVariable: Variable = None

  def containingClass: Template

}


trait TypedMember extends Member {

  def getScalaType: ScalaType

}

object Member {
  implicit def none2empty(t: None.type): Empty.type = Empty


  implicit def holder2option[H <: Member](h: H): Option[H] = h match {
    case Empty => None
    case _ => Some(h)
  }

  def fromContext(implicit ctx: Context): Seq[Member] = {
    ctx.template.members
  }


}