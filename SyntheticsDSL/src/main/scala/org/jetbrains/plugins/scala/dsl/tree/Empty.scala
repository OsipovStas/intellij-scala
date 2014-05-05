package org.jetbrains.plugins.scala.dsl.tree

import org.jetbrains.plugins.scala.dsl.types.{StdTypes, ScalaType, Context}

/**
 * @author stasstels
 * @since  4/29/14.
 */


object Empty extends Value with Variable with ScalaClass  with TypedMember {


  override def add(m: Method)(implicit ctx: Context): Unit = {}

  override def add(methods: Seq[Method])(implicit ctx: Context): Unit = {}

  override def getScalaType: ScalaType = StdTypes.Nothing

  override def containingClass: ScalaClass = this

  override def hasAnnotation(a: Annotation)(implicit ctx: Context): Boolean = false


}