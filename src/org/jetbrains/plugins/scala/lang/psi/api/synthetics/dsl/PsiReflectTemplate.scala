package org.jetbrains.plugins.scala
package lang.psi.api.synthetics.dsl

import org.jetbrains.plugins.scala.dsl.tree.{Method, Empty, Member, Template}
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.{ScObject, ScTrait, ScClass, ScTemplateDefinition}
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.ScNamedElement
import org.jetbrains.plugins.scala.dsl.types.Context

/**
 * @author stasstels
 * @since  4/29/14.
 */
abstract class PsiReflectTemplate(template: ScTemplateDefinition, reference: ScNamedElement) extends Template {
  override def members: Seq[Member] = template.members.map(m => PsiReflectMember(m))

  override def add(m: Method)(implicit ctx: Context): Unit = ctx match {
    case stubCtx: SignatureStubContext => stubCtx.addStub(SignatureStub(m, reference))
  }

}

object PsiReflectTemplate {
  def apply(template: ScTemplateDefinition, reference: ScNamedElement) = template match {
    case clazz: ScClass => PsiReflectClass(clazz, reference)
    case tr: ScTrait => PsiReflectTrait(tr, reference)
    case obj: ScObject => PsiReflectObject(obj, reference)
    case _ => Empty
  }
}