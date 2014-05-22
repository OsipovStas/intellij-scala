package org.jetbrains.plugins.scala
package lang.psi.api.synthetics.dsl

import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.{ScObject, ScTrait, ScClass}
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.ScNamedElement

/**
 * @author stasstels
 * @since  5/21/14.
 */
case class PsiReflectClass(clazz: ScClass, reference: ScNamedElement) extends PsiReflectTemplate(clazz, reference)

case class PsiReflectTrait(tr: ScTrait, reference: ScNamedElement) extends PsiReflectTemplate(tr, reference)

case class PsiReflectObject(obj: ScObject, reference: ScNamedElement) extends PsiReflectTemplate(obj, reference)