package org.jetbrains.plugins.scala
package lang.psi.api.synthetics.dsl

import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.{ScObject, ScTrait, ScClass}
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.ScNamedElement
import org.jetbrains.plugins.scala.dsl.tree.{Empty, Template, ScalaClass}
import org.jetbrains.plugins.scala.lang.psi.ScalaPsiUtil

/**
 * @author stasstels
 * @since  5/21/14.
 */
case class PsiReflectClass(clazz: ScClass, reference: ScNamedElement) extends PsiReflectTemplate(clazz, reference) with ScalaClass {
  override def companion: Template = ScalaPsiUtil.getCompanionModule(clazz).map(PsiReflectTemplate(_, clazz)).getOrElse(Empty)
}

case class PsiReflectTrait(tr: ScTrait, reference: ScNamedElement) extends PsiReflectTemplate(tr, reference) {
  override def companion: Template = Empty
}

case class PsiReflectObject(obj: ScObject, reference: ScNamedElement) extends PsiReflectTemplate(obj, reference) {
  override def companion: Template = Empty
}