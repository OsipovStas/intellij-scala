package org.jetbrains.plugins.scala
package lang.psi.api.annotations.dsl


/**
 * @author stasstels
 * @since  3/30/14.
 */
trait AnnotationHolder {

  def getName: String

  def getType: String

  def getContainingClass: Option[DefinitionsHolder]


}

trait ValueHolder extends AnnotationHolder

trait VarHolder extends AnnotationHolder
