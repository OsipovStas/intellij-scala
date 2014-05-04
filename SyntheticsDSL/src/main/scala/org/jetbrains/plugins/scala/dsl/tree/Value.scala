package org.jetbrains.plugins.scala.dsl.tree

/**
 * @author stasstels
 * @since  4/29/14.
 */

trait Value extends Member  {
  override def asValue: Value = this
  override def isValue: Boolean = true
}

