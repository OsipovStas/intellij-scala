package org.jetbrains.plugins.scala.dsl.tree

/**
 * @author stasstels
 * @since  4/29/14.
 */


object Empty extends Value with Variable with ScalaClass {

  override def name: String = null
  override def containingClass: ScalaClass = this
  override def hasAnnotation(a: Annotation): Boolean = false

  override def add(m: MethodDefinition): Unit = {}

  override def add(s: Seq[MethodDefinition]): Unit = {}
}