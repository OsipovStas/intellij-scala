package org.jetbrains.plugins.scala
package lang.psi.api.synthetics

import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.ScMember
import org.jetbrains.plugins.scala.dsl.tree.{Empty, Member, TypedMember, Method}
import org.jetbrains.plugins.scala.lang.psi.types.{ScType, Signature, ScSubstitutor}
import org.jetbrains.plugins.scala.lang.psi.ScalaPsiUtil
import org.jetbrains.plugins.scala.dsl.types.{Context, ScalaType}
import com.intellij.psi.{PsiNamedElement, PsiElement, PsiMethod}
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.ScModifierListOwner
import org.jetbrains.plugins.scala.lang.psi.types.nonvalue.Parameter
import org.jetbrains.plugins.scala.lang.psi.fake.FakePsiMethod
import org.jetbrains.plugins.scala.lang.psi.api.statements.{ScVariable, ScValue, ScDeclaredElementsHolder}
import org.jetbrains.plugins.scala.lang.psi.impl.ScalaPsiElementFactory
import org.jetbrains.plugins.scala.lang.psi.api.synthetics.dsl.{PsiReflectTypedVariable, PsiReflectTypedValue, PsiReflectVariable, PsiReflectValue}
import org.jetbrains.plugins.scala.lang.psi.api.synthetics.base.{BeanScript, ScSyntheticOwner}
import org.jetbrains.plugins.scala.dsl.base.ScamScript
import com.intellij.openapi.util.text

/**
 * @author stasstels
 * @since  4/29/14.
 */
object SyntheticUtil {

  type Type = ((TypedMember) => ScalaType)

  def register(script: ScamScript): Unit = {
    val scriptName = script.getClass.getCanonicalName
    val scripts = scamScripts.filterNot {
      case s =>
        text.StringUtil.equals(s.getClass.getCanonicalName, scriptName)
    }
    scamScripts = script +: scripts
  }

  def unplug() {
    scamScripts = baseScripts
  }

  def ScalaType2ScType(st: ScalaType, context: PsiElement, child: PsiElement) = ScalaPsiElementFactory.createTypeFromText(st.show, context, child)


  private val baseScripts: Seq[ScamScript] = Seq(BeanScript)

  private var scamScripts = baseScripts

  def scMember2member(m: ScMember): Member = m match {
    case aVal: ScValue => PsiReflectValue(aVal)
    case aVar: ScVariable => PsiReflectVariable(aVar)
    case _ => Empty
  }


  def getNavigator(member: ScMember): PsiElement = member match {
    case d: ScDeclaredElementsHolder => d.declaredElements.headOption.getOrElse(member)
  }


  implicit def member2typed(m: ScMember): TypedMember = m match {
    case aVal: ScValue => PsiReflectTypedValue(aVal)
    case aVar: ScVariable => PsiReflectTypedVariable(aVar)
    case _ => Empty
  }

  def getSignaturesFor(m: ScSyntheticOwner, subst: ScSubstitutor): Seq[SyntheticSignature] = {
    implicit val context = new SyntheticAnalyzerContext(m)
    scamScripts.foreach(_.run())
    context.methods.map(SyntheticSignature(_, subst, m))
  }


  def createFake(siga: SyntheticSignature): PsiMethod = {
    val hasModifierProperty: String => Boolean = siga.member match {
      case v: ScModifierListOwner => v.hasModifierProperty
      case _ => _ => false
    }
    import extensions.toSeqExt
    implicit def arr2arr(a: Array[ScType]): Array[Parameter] = a.toSeq.mapWithIndex {
      case (tpe, index) => new Parameter("", None, tpe, false, false, false, index)
    }.toArray
    val typed = member2typed(siga.member)
    val rType = ScalaType2ScType(siga.methodDef.returnType(typed), siga.member.getContext, siga.member)
    val types = siga.methodDef.parameters.map {
      case t => ScalaType2ScType(t(typed), siga.member.getContext, siga.member)
    }.toArray
    new FakePsiMethod(SyntheticUtil.getNavigator(siga.member), siga.name, types, rType, hasModifierProperty)
  }


  class SyntheticAnalyzerContext(m: ScMember) extends Context {

    implicit def ScalaType2ScType(t: ScalaType): ScType = ScalaPsiElementFactory.createTypeFromText(t.show, m.getContext, m)

    override def equiv(st1: ScalaType, st2: ScalaType): Boolean = st1 equiv st2

    override def conform(st1: ScalaType, st2: ScalaType): Boolean = st1 conforms st2

    var methods: Seq[Method] = Seq.empty


    override val shouldResolveAnnotation: Boolean = false

    override def member: Member = scMember2member(m)

    def add(m: Method) = {
      methods = m +: methods
    }

  }

  case class SyntheticSignature(methodDef: Method, subst: ScSubstitutor, member: ScSyntheticOwner) extends {
    val myName = methodDef.name(member.name)
    val params = ScalaPsiUtil.getTypesStream[Type](methodDef.parameters, (t: Type) => {
      ScalaType2ScType(t(member), member.getContext, member)
    })
    val paramsLength: Int = methodDef.parameters.length
    val named = SyntheticUtil.getNavigator(member) match {
      case n: PsiNamedElement => Some(n)
      case _ => None
    }
  } with Signature(myName, params, paramsLength, subst, named)

}
