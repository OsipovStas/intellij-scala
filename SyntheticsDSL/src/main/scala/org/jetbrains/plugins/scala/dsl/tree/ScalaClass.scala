package org.jetbrains.plugins.scala.dsl.tree

import org.jetbrains.plugins.scala.dsl.types.Context

/**
 * @author stasstels
 * @since  4/29/14.
 */
trait ScalaClass {
  def add(m: Method)(implicit ctx: Context) {
    println(m)
  }

  def add(methods: Seq[Method])(implicit ctx: Context) {
    methods.foreach(add)
  }
}
