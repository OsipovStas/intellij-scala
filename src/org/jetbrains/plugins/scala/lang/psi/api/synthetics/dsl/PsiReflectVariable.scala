package org.jetbrains.plugins.scala
package lang.psi.api.synthetics.dsl

import org.jetbrains.plugins.scala.lang.psi.api.statements.ScVariable
import org.jetbrains.plugins.scala.dsl.tree.{Template, Variable}
import org.jetbrains.plugins.scala.dsl.types.ScalaType
import org.jetbrains.plugins.scala.lang.psi.types.result.TypingContext

/**
 * @author stasstels
 * @since  4/29/14.
 */
case class PsiReflectVariable(v: ScVariable) extends PsiReflectMember(v) with Variable {

  override def getScalaType: ScalaType = v.getType(TypingContext.empty).getOrAny.asScalaType

  override def containingClass: Template = PsiReflectTemplate(v.containingClass, v.declaredElements.head)
}
