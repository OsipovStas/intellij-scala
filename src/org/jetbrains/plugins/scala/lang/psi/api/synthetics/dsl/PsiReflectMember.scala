package org.jetbrains.plugins.scala
package lang.psi.api.synthetics.dsl

import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.ScMember
import org.jetbrains.plugins.scala.dsl.tree._
import org.jetbrains.plugins.scala.lang.psi.api.statements.{ScValue, ScVariable, ScAnnotationsHolder}
import org.jetbrains.plugins.scala.dsl.types.Context

/**
 * @author stasstels
 * @since  4/29/14.
 */
abstract class PsiReflectMember(m: ScMember) extends TypedMember {
  override def hasAnnotation(a: Annotation)(implicit ctx: Context): Boolean = {
    m match {
      case holder: ScAnnotationsHolder =>
        holder.annotations.exists {
          case annot =>
            val ann = annot.typeElement.getText.replace(" ", "")
            ann.endsWith("." + a.qualifiedName) || ann.equals(a.qualifiedName)
        }
      case _ => false
    }
  }
}




object PsiReflectMember {
  def apply(m: ScMember): TypedMember = m match {
    case aVar: ScVariable => PsiReflectVariable(aVar)
    case aVal: ScValue => PsiReflectValue(aVal)
    case _ => Empty
  }
}
