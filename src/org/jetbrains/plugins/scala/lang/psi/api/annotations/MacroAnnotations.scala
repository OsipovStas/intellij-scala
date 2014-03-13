package org.jetbrains.plugins.scala
package lang.psi.api.annotations

import org.jetbrains.plugins.scala.lang.psi.{ScalaPsiUtil, types, fake}
import fake.FakePsiMethod
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiMethod
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.{ScTypedDefinition, ScModifierListOwner, ScNamedElement}
import org.jetbrains.plugins.scala.lang.psi.types.{ScSubstitutor, Signature}
import org.jetbrains.plugins.scala.lang.psi.api.statements.{ScVariable, ScValue, ScAnnotationsHolder}
import org.jetbrains.plugins.scala.lang.psi.api.statements.params.ScClassParameter

/**
 * @author stasstels
 * @since  3/11/14.
 *
 */
object MacroAnnotations {

  def isFakeProperty(s: ScAnnotationsHolder): Boolean = {
    s.annotations.exists {
      case annot => StringUtil.containsIgnoreCase(annot.typeElement.getText, "fake")
    }
  }

  
  def getFakeMethod(x: ScNamedElement): PsiMethod = {
    val hasModifierProperty: String => Boolean = ScalaPsiUtil.nameContext(x) match {
      case v: ScModifierListOwner => v.hasModifierProperty
      case _ => _ => false
    }
    new FakePsiMethod(x, "fake" + StringUtil.capitalize(x.name), Array.empty, types.Boolean, hasModifierProperty)
  }

  def getFakeMethods(s: ScTypedDefinition): Seq[PsiMethod] = {
    def fakeSeq(v: ScAnnotationsHolder with ScModifierListOwner): Seq[PsiMethod] = {
      val fakeProperty: Boolean = isFakeProperty(v)
      if (fakeProperty) {
        Seq(MacroAnnotations.getFakeMethod(s))
      } else Seq.empty
    }
    ScalaPsiUtil.nameContext(s) match {
      case v: ScValue =>
        fakeSeq(v)
      case _ => Seq.empty
    }
  }
  
  
  def getFakeSignature(dcl: ScTypedDefinition, subst: ScSubstitutor): Signature = new Signature("fake" + dcl.name.capitalize, Stream.empty, 0, subst, Some(dcl))

}
