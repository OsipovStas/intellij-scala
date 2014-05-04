package org.jetbrains.plugins.scala
package lang.psi.api.synthetics.dsl

import org.jetbrains.plugins.scala.lang.psi.api.statements.ScVariable
import org.jetbrains.plugins.scala.dsl.tree.Variable
import org.jetbrains.plugins.scala.dsl.types.ScalaType
import org.jetbrains.plugins.scala.lang.psi.types.result.TypingContext

/**
 * @author stasstels
 * @since  4/29/14.
 */
case class PsiReflectVariable(v: ScVariable) extends PsiReflectMember(v) with Variable


case class PsiReflectTypedVariable(v: ScVariable) extends  PsiReflectTypedMember(v) with Variable {
  override def getScalaType: ScalaType = v.getType(TypingContext.empty).getOrAny.asScalaType
}