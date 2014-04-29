package org.jetbrains.plugins.scala.dsl.tree

/**
 * @author stasstels
 * @since  4/29/14.
 */
trait ScalaClass {
  def add(m: MethodDefinition) {
    println(m)
  }

  def add(s: Seq[MethodDefinition]) {
    println(s)
  }
}
