package org.jetbrains.plugins.scala.dsl

import org.jetbrains.plugins.scala.dsl.tree.Member
import org.jetbrains.plugins.scala.dsl.types.Type


/**
 * @author stasstels
 * @since  4/29/14.
 */

trait Context {
  def member: Member

  def typeContext: TypeContext
}

trait TypeContext {

  def returnType: Option[Type]

}