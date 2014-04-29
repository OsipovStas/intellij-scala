package org.jetbrains.plugins.scala.dsl.tree

/**
 * @author stasstels
 * @since  4/29/14.
 */

trait AnnotationHolder {
  def hasAnnotation(a: Annotation): Boolean

}
