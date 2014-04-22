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
import org.jetbrains.plugins.scala.lang.psi.api.statements.ScFunction
import org.jetbrains.plugins.scala.lang.psi.impl.ScalaPsiElementFactory
import org.jetbrains.plugins.scala.lang.psi.api.annotations.SyntheticUtils
import org.jetbrains.plugins.scala.lang.psi.api.annotations.dsl.AnnotationHolder

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

  import org.jetbrains.plugins.scala.lang.psi.api.annotations.dsl.MacroAnnotation._

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
