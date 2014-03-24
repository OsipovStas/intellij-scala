package org.jetbrains.plugins.scala
package lang.psi.api.annotations

import org.jetbrains.plugins.scala.lang.psi.api.toplevel.{ScModifierListOwner, ScTypedDefinition}
import com.intellij.psi.PsiMethod
import org.jetbrains.plugins.scala.lang.psi.types.{ScType, Signature, ScSubstitutor}
import org.jetbrains.plugins.scala.lang.psi.light.PsiTypedDefinitionWrapper.DefinitionRole._
import org.jetbrains.plugins.scala.lang.psi.ScalaPsiUtil
import org.jetbrains.plugins.scala.lang.psi.types.nonvalue.Parameter
import extensions.toSeqExt

/**
 * @author stasstels
 * @since  3/24/14.
 */
trait SyntheticMemberCreator {

  def transformedName(name: String): String

  def checkName(name: String): Boolean

  def createMember(definition: ScTypedDefinition): PsiMethod

  def createSignature(definition: ScTypedDefinition, substitutor: ScSubstitutor): Signature

  def getRole: DefinitionRole

  def hasModifierProperty(definition: ScTypedDefinition): String => Boolean = ScalaPsiUtil.nameContext(definition) match {
    case v: ScModifierListOwner => v.hasModifierProperty
    case _ => _ => false
  }

  implicit def arr2arr(a: Array[ScType]): Array[Parameter] = a.toSeq.mapWithIndex {
    case (tpe, index) => new Parameter("", None, tpe, false, false, false, index)
  }.toArray


}


