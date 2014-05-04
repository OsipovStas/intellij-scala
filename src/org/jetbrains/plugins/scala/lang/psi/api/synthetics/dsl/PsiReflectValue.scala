package org.jetbrains.plugins.scala
package lang.psi.api.synthetics.dsl

import org.jetbrains.plugins.scala.lang.psi.api.statements.ScValue
import org.jetbrains.plugins.scala.dsl.tree.Value
import org.jetbrains.plugins.scala.dsl.types.ScalaType
import org.jetbrains.plugins.scala.lang.psi.types.result.TypingContext

/**
 * @author stasstels
 * @since  4/29/14.
 */
case class PsiReflectValue(v: ScValue) extends PsiReflectMember(v) with Value

case class PsiReflectTypedValue(v: ScValue) extends PsiReflectTypedMember(v) with Value {

  override def getScalaType: ScalaType = v.getType(TypingContext.empty).getOrAny.asScalaType
}
