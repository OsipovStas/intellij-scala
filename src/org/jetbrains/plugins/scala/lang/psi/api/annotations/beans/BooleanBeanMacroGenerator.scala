package org.jetbrains.plugins.scala
package lang.psi.api.annotations.beans

import org.jetbrains.plugins.scala.lang.psi.api.annotations.{SyntheticMemberCreator, MacroGenerator}
import com.intellij.psi.{PsiMethod, PsiNamedElement}
import org.jetbrains.plugins.scala.lang.psi.api.statements.{ScVariable, ScValue, ScAnnotationsHolder}
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.{ScTypedDefinition, ScModifierListOwner}
import org.jetbrains.plugins.scala.lang.psi.{types, ScalaPsiUtil}
import org.jetbrains.plugins.scala.lang.psi.api.statements.params.ScClassParameter
import scala.Some
import org.jetbrains.plugins.scala.lang.psi.light.PsiTypedDefinitionWrapper.DefinitionRole
import org.jetbrains.plugins.scala.lang.psi.light.PsiTypedDefinitionWrapper.DefinitionRole.DefinitionRole
import org.jetbrains.plugins.scala.lang.psi.types.{Signature, ScSubstitutor, ScType}
import org.jetbrains.plugins.scala.lang.psi.types.result.TypingContext
import org.jetbrains.plugins.scala.lang.psi.fake.FakePsiMethod
import com.intellij.openapi.util.text.StringUtil

/**
 * @author stasstels
 * @since  3/24/14.
 */
case object BooleanBeanMacroGenerator extends MacroGenerator {


  override protected val creators: Seq[SyntheticMemberCreator] = Seq(IsGetterCreator, SetterCreator)

  override def getCreatorsFor(definition: PsiNamedElement, noResolve: Boolean = false): Seq[SyntheticMemberCreator] = {
    def valueSeq(v: ScAnnotationsHolder with ScModifierListOwner): Seq[SyntheticMemberCreator] = {
      if (checkAnnotation(v, noResolve)) {
        Seq(IsGetterCreator)
      } else Seq.empty
    }
    def variableSeq(v: ScAnnotationsHolder with ScModifierListOwner): Seq[SyntheticMemberCreator] = {
      if (checkAnnotation(v, noResolve)) {
        Seq(IsGetterCreator, SetterCreator)
      } else Seq.empty
    }

    ScalaPsiUtil.nameContext(definition) match {
      case v: ScValue =>
        valueSeq(v)
      case v: ScVariable =>
        variableSeq(v)
      case v: ScClassParameter if v.isVal =>
        valueSeq(v)
      case v: ScClassParameter if v.isVar =>
        variableSeq(v)
      case _ => Seq.empty
    }
  }

  override def checkAnnotation(s: ScAnnotationsHolder, noResolve: Boolean): Boolean = ScalaPsiUtil.isBooleanBeanProperty(s, noResolve)

  case object IsGetterCreator extends SyntheticMemberCreator {


    override def getRole: DefinitionRole = DefinitionRole.IS_GETTER

    override def createMember(definition: ScTypedDefinition): PsiMethod = {
      val tType: ScType = definition.getType(TypingContext.empty).getOrAny
      new FakePsiMethod(definition, transformedName(definition.name), Array.empty,
        tType, hasModifierProperty(definition))
    }

    override def checkName(name: String): Boolean = name.startsWith("is")

    override def transformedName(name: String): String = "is" + StringUtil.capitalize(name)

    override def createSignature(definition: ScTypedDefinition, substitutor: ScSubstitutor): Signature = new Signature(transformedName(definition.name), Stream.empty, 0, substitutor, Some(definition))
  }


  case object SetterCreator extends SyntheticMemberCreator {


    override def getRole: DefinitionRole = DefinitionRole.SETTER


    override def createMember(definition: ScTypedDefinition): PsiMethod = {
      val tType = definition.getType(TypingContext.empty).getOrAny
      new FakePsiMethod(definition, transformedName(definition.name), arr2arr(Array[ScType](tType)), types.Unit, hasModifierProperty(definition))
    }

    override def checkName(name: String): Boolean = name.startsWith(name)

    override def transformedName(name: String): String = "set" + StringUtil.capitalize(name)

    override def createSignature(definition: ScTypedDefinition, substitutor: ScSubstitutor): Signature = new Signature(transformedName(definition.name), ScalaPsiUtil.getSingletonStream(definition.getType(TypingContext.empty).getOrAny), 1,
      substitutor, Some(definition))
  }


}