package org.jetbrains.plugins.scala.dsl.tree

import org.jetbrains.plugins.scala.dsl.types.Context

/**
 * @author stasstels
 * @since  4/29/14.
 */
trait Template {

  def members: Seq[Member]

  def add(m: Method)(implicit ctx: Context): Unit

  def add(ms: Seq[Method])(implicit ctx: Context): Unit = ms.foreach(add)

  def +=(m: Method)(implicit ctx: Context): Unit = add(m)

  def ++=(ms: Seq[Method])(implicit ctx: Context):Unit = add(ms)

  def companion: Template

}

object Template {

  def fromContext(implicit ctx: Context) = ctx.template

}


trait ScalaClass extends Template

trait ScalaObject extends Template
