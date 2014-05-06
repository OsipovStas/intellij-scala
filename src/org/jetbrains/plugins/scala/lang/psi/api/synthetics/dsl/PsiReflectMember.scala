package org.jetbrains.plugins.scala
package lang.psi.api.synthetics.dsl

import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.ScMember
import org.jetbrains.plugins.scala.dsl.tree.{Member, TypedMember, ScalaClass, Annotation}
import org.jetbrains.plugins.scala.lang.psi.api.statements.{ScDeclaredElementsHolder, ScAnnotationsHolder}
import org.jetbrains.plugins.scala.dsl.types.Context

/**
 * @author stasstels
 * @since  4/29/14.
 */
abstract class PsiReflectMember(m: ScMember
        with ScAnnotationsHolder
        with ScDeclaredElementsHolder) extends Member {


  override def hasAnnotation(a: Annotation)(implicit ctx: Context): Boolean = if (ctx.shouldResolveAnnotation) {
    m.hasAnnotation(a.qualifiedName).isDefined
  } else {
    m.annotations.exists {
      case annot =>
        val ann = annot.typeElement.getText.replace(" ", "")
        ann.endsWith("." + a.qualifiedName) || ann.equals(a.qualifiedName)
    }
  }

  override def containingClass: ScalaClass = PsiReflectScalaClass(m.containingClass)

  def name: String = m.declaredElements.headOption.fold("")(_.getName)

}

abstract class PsiReflectTypedMember(m: ScMember
        with ScAnnotationsHolder
        with ScDeclaredElementsHolder) extends PsiReflectMember(m) with TypedMember