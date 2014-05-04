package org.jetbrains.plugins.scala.dsl.tree

import org.jetbrains.plugins.scala.dsl.types.Context


/**
 * @author stasstels
 * @since  4/29/14.
 */

trait AnnotationHolder {
  def hasAnnotation(a: Annotation)(implicit ctx: Context): Boolean

}
