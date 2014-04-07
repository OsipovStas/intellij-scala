package org.jetbrains.plugins.scala
package lang.psi.api.annotations.base

import org.jetbrains.plugins.scala.lang.psi.api.annotations.typedef.SyntheticOwner
import com.intellij.psi._
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.{ScModifierListOwner, ScTypedDefinition}
import org.jetbrains.plugins.scala.lang.psi.ScalaPsiUtil
import org.jetbrains.plugins.scala.lang.psi.types.ScType
import org.jetbrains.plugins.scala.lang.psi.light.PsiTypedDefinitionWrapper.DefinitionRole
import org.jetbrains.plugins.scala.lang.psi.light.PsiTypedDefinitionWrapper.DefinitionRole.DefinitionRole
import org.jetbrains.plugins.scala.lang.psi.fake.FakePsiMethod
import extensions.toSeqExt
import javax.swing.Icon
import org.jetbrains.plugins.scala.icons.Icons
import org.jetbrains.plugins.scala.lang.psi.types.nonvalue.Parameter
import org.jetbrains.plugins.scala.lang.psi.api.annotations.dsl.MacroAnnotation.Method
import org.jetbrains.plugins.scala.lang.psi.api.annotations.dsl.AnnotationHolder
import org.jetbrains.plugins.scala.lang.psi.api.statements.ScFunction
import org.jetbrains.plugins.scala.lang.psi.impl.ScalaPsiElementFactory
import org.jetbrains.plugins.scala.lang.psi.api.annotations.SyntheticUtils

/**
 * @author stasstels
 * @since  4/2/14.
 */
trait FakeCreator {



  def transformedName(owner: String): String

  def createMember(holder: SyntheticOwner): ScFunction

  def getRole: DefinitionRole

  def hasModifierProperty(definition: ScTypedDefinition): String => Boolean = ScalaPsiUtil.nameContext(definition) match {
    case v: ScModifierListOwner => v.hasModifierProperty
    case _ => _ => false
  }
}


object FakeCreator {

  implicit def arr2arr(a: Array[ScType]): Array[Parameter] = a.toSeq.mapWithIndex {
    case (tpe, index) => new Parameter("", None, tpe, false, false, false, index)
  }.toArray

  private class SyntheticPsiMethod(navElement: PsiElement,
                                   name: String,
                                   params: Array[Parameter],
                                   retType: ScType,
                                   hasModifier: String => Boolean) extends FakePsiMethod(navElement, name, params, retType, hasModifier) {




    override def getIcon(flags: Int): Icon = {
      Icons.FUNCTION
    }
  }


  def apply(m: Method) = new FakeCreator {

    def methodText(holder: SyntheticOwner): String = {
      val aHolder: AnnotationHolder = holder.asAnnotationHolder
      val name = m.name(aHolder.getName)
      val params = SyntheticUtils.getParameterString(m.params(aHolder))
      val rType = m.returnType(aHolder)
      s"def $name$params: $rType = new Error()"
    }

    override def getRole: DefinitionRole.DefinitionRole = DefinitionRole.SIMPLE_ROLE

    override def createMember(holder: SyntheticOwner): ScFunction = {

      val clazz = holder.getContainingClass
      val fun = ScalaPsiElementFactory.createMethodWithContext(methodText(holder), clazz.getContext, clazz)
      fun.setSynthetic(holder)
      fun

    }

    override def transformedName(baseName: String): String = m.name(baseName)
  }



}
