package org.jetbrains.plugins.scala.dsl.tree

/**
 * @author stasstels
 * @since  4/29/14.
 */
trait Variable extends Member {

  override def asVariable: Variable = this
  override def isVariable: Boolean = true
}