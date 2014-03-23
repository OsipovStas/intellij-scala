package org.jetbrains.plugins.scala
package lang.psi.api.annotations

import com.intellij.psi.{PsiNamedElement, PsiMethod}
import org.jetbrains.plugins.scala.lang.psi.api.statements.{ScVariable, ScValue, ScAnnotationsHolder}
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.{ScTypedDefinition, ScModifierListOwner}
import org.jetbrains.plugins.scala.lang.psi.{types, ScalaPsiUtil}
import org.jetbrains.plugins.scala.lang.psi.api.statements.params.ScClassParameter
import org.jetbrains.plugins.scala.lang.psi.fake.FakePsiMethod
import com.intellij.openapi.util.text.StringUtil
import org.jetbrains.plugins.scala.lang.psi.types.result.TypingContext
import org.jetbrains.plugins.scala.lang.psi.types.{Signature, ScSubstitutor, ScType}
import org.jetbrains.plugins.scala.lang.psi.types.nonvalue.Parameter
import extensions.toSeqExt
import org.jetbrains.plugins.scala.lang.psi.light.PsiTypedDefinitionWrapper.DefinitionRole
import org.jetbrains.plugins.scala.lang.psi.light.PsiTypedDefinitionWrapper.DefinitionRole.DefinitionRole

/**
 * @author stasstels
 * @since  3/19/14.
 */
trait Generator {

  def checkAnnotation(s: ScAnnotationsHolder, noResolve: Boolean = false): Boolean

  def hasSuitableCreator(baseName: String, decodedName: String): Boolean = {
    creators.exists {
      case creator => StringUtil.equals(creator.transformedName(baseName), decodedName)
    }
  }

  def findSuitableCreators(baseName: String, decodedName: String): Seq[SyntheticMemberCreator] = {
    creators.filter {
      case creator => StringUtil.equals(creator.transformedName(baseName), decodedName)
    }
  }


  def getCreatorsFor(definition: PsiNamedElement, noResolve: Boolean = false): Seq[SyntheticMemberCreator]
  

  protected val creators: Seq[SyntheticMemberCreator]
}

trait SyntheticMemberCreator {

  def transformedName(name: String): String

  def checkName(name: String): Boolean

  def createMember(definition: ScTypedDefinition): PsiMethod

  def createSignature(definition: ScTypedDefinition, substitutor: ScSubstitutor): Signature

  def getRole: DefinitionRole

  def getKey: (Generator, Int)

  def hasModifierProperty(definition: ScTypedDefinition): String => Boolean = ScalaPsiUtil.nameContext(definition) match {
    case v: ScModifierListOwner => v.hasModifierProperty
    case _ => _ => false
  }

  implicit def arr2arr(a: Array[ScType]): Array[Parameter] = a.toSeq.mapWithIndex {
    case (tpe, index) => new Parameter("", None, tpe, false, false, false, index)
  }.toArray


}



case object BeanGenerator extends Generator {


  override protected val creators: Seq[SyntheticMemberCreator] = Seq(GetterCreator, SetterCreator)

  override def getCreatorsFor(definition: PsiNamedElement, noResolve: Boolean = false): Seq[SyntheticMemberCreator] = {
    def valueSeq(v: ScAnnotationsHolder with ScModifierListOwner): Seq[SyntheticMemberCreator] = {
      if (checkAnnotation(v, noResolve)) {
        Seq(GetterCreator)
      } else Seq.empty
    }
    def variableSeq(v: ScAnnotationsHolder with ScModifierListOwner): Seq[SyntheticMemberCreator] = {
      if (checkAnnotation(v, noResolve)) {
        Seq(GetterCreator, SetterCreator)
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

  override def checkAnnotation(s: ScAnnotationsHolder, noResolve: Boolean): Boolean = ScalaPsiUtil.isBeanProperty(s, noResolve)

  case object GetterCreator extends SyntheticMemberCreator {


    override def getRole: DefinitionRole = DefinitionRole.GETTER

    override def getKey: (Generator, Int) = (BeanGenerator, 0)

    override def createMember(definition: ScTypedDefinition): PsiMethod = {
      val tType: ScType = definition.getType(TypingContext.empty).getOrAny
      new FakePsiMethod(definition, transformedName(definition.name), Array.empty,
        tType, hasModifierProperty(definition))
    }

    override def checkName(name: String): Boolean = name.startsWith("get")

    override def transformedName(name: String): String = "get" + StringUtil.capitalize(name)

    override def createSignature(definition: ScTypedDefinition, substitutor: ScSubstitutor): Signature = new Signature(transformedName(definition.name), Stream.empty, 0, substitutor, Some(definition))
  }


  case object SetterCreator extends SyntheticMemberCreator {


    override def getRole: DefinitionRole = DefinitionRole.SETTER

    override def getKey: (Generator, Int) = (BeanGenerator, 1)

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